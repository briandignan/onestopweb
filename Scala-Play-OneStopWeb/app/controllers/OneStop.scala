package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._
import play.api.templates.HtmlFormat.Appendable
import anorm._

import models._
import views._

object OneStop extends Controller with Secured {

	def index = authenticatedPage( html.index( _ ) )
	def inventoryList = authenticatedPage( html.inventoryList( _ ) )
	def customerList = authenticatedPage( html.customerList( _ ) )
	def vendorList = authenticatedPage( html.vendorList( _ ) )
	def reports = authenticatedPage( html.reports( _ ) )
	def settingsOther = authenticatedPage( html.settingsOther( _ ) )

	private def authenticatedPage( f: User => Appendable ): EssentialAction = IsAuthenticated { username =>
		_ =>
			User.findByEmail( username ).map { user =>
				Ok( f( user ) )
			}.getOrElse( Forbidden )
	}

}