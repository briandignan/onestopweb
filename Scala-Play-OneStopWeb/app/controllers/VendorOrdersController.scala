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

object VendorOrdersController extends Controller with Secured {

	
	def list = IsAuthenticated { username =>
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok( html.vendorOrdersList( user, OrdersByVendor.list ) )
			}.getOrElse( Forbidden )
		}
	}
	
	
	def vendor( id: Long ) = IsAuthenticated { username =>
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok( html.vendorSpecificOrdersList( user, VendorOrder.list( id ) ) )
			}.getOrElse( Forbidden )
		}
	}
	
	
	def editOrder( vendorId: Long, orderId: Long ) = IsAuthenticated { username =>
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok( "ORDERS: edit vendor %d order %d".format( vendorId, orderId ) )
			}.getOrElse( Forbidden )
		}
	}
	
	
	def updateOrder( vendorId: Long, orderId: Long ) = IsAuthenticated { username =>
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok( "ORDERS: update order %d for vendor %d".format( orderId, vendorId ) )
			}.getOrElse( Forbidden )
		}
	}
	
	
	def createOrder( vendorId: Long ) = IsAuthenticated { username =>
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok( "ORDERS: create new order for vendor %d".format( vendorId ) )
			}.getOrElse( Forbidden )
		}
	}
	
	
	def executeOrder( vendorId: Long ) = IsAuthenticated { username =>
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok( "ORDERS: execute order for vendor %d".format( vendorId ) )
			}.getOrElse( Forbidden )
		}
	}
	
	
	
	
}