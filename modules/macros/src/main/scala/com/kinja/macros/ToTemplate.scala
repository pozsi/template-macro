package com.kinja.macros

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

class ToTemplate(templateName: String*) extends StaticAnnotation {
	def macroTransform(annottees: Any*): Any = macro ToTemplate.impl
}

object ToTemplate {

	def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
		import c.universe._

		val templateName = c.prefix.tree match {
			case q"new $name( ..$params )" => params match {
				case l: List[c.universe.Tree] => l.headOption match {
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
			val template = tq"com.kinja.mantle.template.Template"
			
			val tuples = params.map {
				case field @ ValDef(_, _, typeTree,_) if false =>
					println("jjajajaj")
					q"""${field.name.toString} -> this.${TermName(field.name.toString)}.render(locale, features)"""
				case field@ValDef(_, _, typeTree, _) =>
					println(typeTree.tpe)
					q"""${field.name.toString} -> this.${TermName(field.name.toString)}"""
			}

			c.Expr[Any](
				q"""
        case class $className(..$params) extends Template(..$templateName) with ..$parents {
          ..$body
					def render(locale: String, features: List[String]): String = {
						val map = Map(..$tuples)
						Closure.render(locale, features, underlying, map)
					}
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