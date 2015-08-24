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
import java.util.Date
import java.text.SimpleDateFormat


object CustomerSalesController extends Controller with Secured {

	
	val home = Redirect( routes.CustomerSalesController.list() )
	
	val createCustomer = Form(
		mapping(
			"customerOrderId" -> optional(longNumber),
			"customerId" -> optional(longNumber),
			"dateTime" -> date("yyyy-MM-dd HH:mm:ss"),
			"items" -> seq(
				mapping(
					"customerOrderId" -> optional(longNumber),
					"itemId" -> longNumber,
					"quantity" -> longNumber,
					"unitPrice" -> bigDecimal
				)(CustomerOrderItem.apply)(CustomerOrderItem.unapply)
			)
		)(CustomerOrder.apply)(CustomerOrder.unapply)
	)
	
	
	
	def list() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok( html.customerSalesList( user, CustomerOrderSummary.list ) )
			}.getOrElse( Forbidden )
		}
	}
	
	def create() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				Ok( html.customerSalesCreate( user, createCustomer.bind(Map("dateTime"->sdf.format(new Date))), Customer.options, Item.options, Item.prices ) )
			}.getOrElse( Forbidden )
		}
	}
	
	def add() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				createCustomer.bindFromRequest.fold(
					formWithErrors => BadRequest( html.customerSalesCreate( user, formWithErrors, Customer.options, Item.options, Item.prices ) ),
					customerOrder => {
						CustomerOrder.create( customerOrder )
						customerOrder.items.foreach { item =>
							Item.decreaseCount( item.itemId, item.quantity )
						}
						home.flashing( "success" -> "The transaction was added" )
					}
				)
			}.getOrElse( Forbidden )
		}
	}
	
	def view(id: Long) = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				CustomerOrder.findById(id).map { order => 
					Ok( html.customerSalesView ( user, createCustomer.fill( order ), Customer.options, Item.options ) )
				}.getOrElse( NotFound )
			}.getOrElse( Forbidden )
		}
	}
	
	def delete(id: Long) = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok
			}.getOrElse( Forbidden )
		}
	}
}