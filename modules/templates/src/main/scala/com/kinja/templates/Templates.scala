package com.kinja.templates

import com.kinja.macros.ToTemplate
import com.kinja.macros.AutoRender

@ToTemplate("content.html")
case class Content(
	fejlec: String,
	szoveg: String)


@ToTemplate("page.html")
case class Page(
	cim: String,
	tartalom: Content)

abstract class Template(val underlying: String) {
	def render(locale: String, features: List[String]): String
}

object Closure {
	def render(locale: String, features: List[String], templateName: String, data: Map[String, Any]): String = {
		s"""$templateName
$locale
$features
$data"""
	}
}
