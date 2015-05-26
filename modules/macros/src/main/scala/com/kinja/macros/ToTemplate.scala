package com.kinja.macros

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.Context

class ToTemplate(templateName: String*) extends StaticAnnotation {
	def macroTransform(annottees: Any*): Any = macro ToTemplate.impl
}

object ToTemplate {

	def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
		import c.universe._

		val templateName = c.prefix.tree match {
			case q"new $name( ..$params )" => params match {
				case l: List[Tree] => l.headOption match {
					case Some(Literal(Constant(templateName: String))) => templateName
					case _ => throw new Exception("template name must be specified.")
				}
			}
			case _ => throw new Exception("template name must be specified.")
		}

		def caseClassParts(classDecl: ClassDef) = classDecl match {
			case q"case class $className(..$fields) extends ..$parents { ..$body }" =>
				(className, fields, parents, body)
		}

		def modifiedDeclaration(classDecl: ClassDef) = {

			val (className, fields, parents, body) = caseClassParts(classDecl)
			val params = fields.asInstanceOf[List[ValDef]]

			val tuples = params.map {
				case field@ValDef(_, _, typeTree, _) =>
					q"""${field.name.toString} -> this.${newTermName(field.name.toString)}"""
			}

			c.Expr[Any](
				q"""
        case class $className(..$params) extends Template(..$templateName) with ..$parents {
          ..$body
					def render(locale: String, features: List[String]): String = AutoRender.renderTemplate(locale, features, ..$tuples)
        }
      """
			)
		}

		annottees.map(_.tree).toList match {
			case (classDecl: ClassDef) :: Nil => modifiedDeclaration(classDecl)
			case _ => c.abort(c.enclosingPosition, "Invalid annottee")
		}
	}
}

object AutoRender {
	
	def renderTemplate(locale: String, features: List[String], tuples: (String, Any)*) = macro impl

	def impl(c: Context)(locale: c.Expr[String], features: c.Expr[List[String]], tuples: c.Expr[(String, Any)]*): c.Expr[String] = {

		import c.universe._

		val template = Select(Select(Select(Ident(newTermName("com")), newTermName("kinja")), newTermName("templates")), newTermName("Template"))

		val newTuples = tuples.map { tuple =>

			// template.tpe is null here
			// val isTemplate = tuple.tree.children.head.children.tail.head.tpe <:< template.tpe
			val isTemplate = tuple.tree.children.head.children.tail.head.toString.startsWith("com.kinja.templates")

			if (isTemplate) {
				// clearly I don't know what I'm doing here, but it works.
				val key = tuple.tree.children.head.children.head.children.head
				val value = tuple.tree.children.tail.head
				val newTuple = c.Expr[Nothing](q"""$key->[String]$value.render(locale, features)""")
				newTuple
			} else {
				tuple
			}

		}

		c.Expr[String](q"""
			val map = Map(..$newTuples) ++ Map("locale" -> locale, "features" -> features)
			Closure.render(locale, features, underlying, map)
		""")

	}

}
