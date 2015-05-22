package com.kinja.mantle

import com.kinja.templates.{Content, Page}

object Main {

	def main(args: Array[String]) {
		val page = Page("ez a cim", Content("valami", "ide is"))
		val html = page.render("en-us", List("egyik feature", "masik feature"))
		println(html)
	}

}
