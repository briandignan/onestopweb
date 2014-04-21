package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.validation.Constraint
import play.api.data.validation.ValidationResult
import play.api.data.validation.Valid
import play.api.data.validation.Invalid
import play.api.data.Forms._
import play.api.templates.HtmlFormat.Appendable

import anorm._

import models._
import views._

object VendorPurchasesController extends Controller with Secured {

	def list() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok( html.vendorPurchasesList( user ) )
			}.getOrElse( Forbidden )
		}
	}
		
}