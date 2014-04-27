package controllers

//import play.api._
/*
import play.api.data._
import play.api.data.validation.Constraint
import play.api.data.validation.ValidationResult
import play.api.data.validation.Valid
import play.api.data.validation.Invalid
import play.api.data.Forms._
import play.api.templates.HtmlFormat.Appendable
import play.api.mvc.Controller

import anorm._

import models._
import views._
*/



import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import views._
import models._
import anorm.Pk
import anorm.NotAssigned


object CustomerManagementController extends Controller with Secured {

	val home = Redirect( routes.CustomerManagementController.list )
	
	val editCustomerForm: Form[Customer] = Form(
		mapping(
			"id" -> ignored(NotAssigned:Pk[Long]),
			"firstName" -> nonEmptyText,
			"lastName" -> nonEmptyText,
			"email" -> optional(text),
			"phone" -> optional(text), //TODO Verify that it's a phone number
			"promotionDevice" -> longNumber,
			"favorites" -> seq(longNumber) 
		)(Customer.apply)(Customer.unapply)
	)
	
	def list() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok( html.customerManagementList( user, Customer.findAll ) ) 
			}.getOrElse( Forbidden )
		}
	}
	
	def edit(id: Long) = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				Customer.findById(id) match {
					case Some( customer ) => {
						Ok( html.customerManagementEdit( user, id, editCustomerForm.fill(customer), PromotionDevice.options, ProductType.options ) )
					} 
					case None => NotFound
				}
			}.getOrElse( Forbidden )
		}
	}
	
	def update(id: Long) = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				editCustomerForm.bindFromRequest.fold( 
					formWithErrors => BadRequest( html.customerManagementEdit( user, id, formWithErrors, PromotionDevice.options, ProductType.options ) ),
					customerToUpdate => {
						Customer.update( id, customerToUpdate )
						home.flashing( "success" -> "%s %s was updated".format( customerToUpdate.firstName, customerToUpdate.lastName ) )
					}
				)
			}.getOrElse( Forbidden )
		}
	}
	
	def create() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok( html.customerManagementCreate( user, editCustomerForm, PromotionDevice.options, ProductType.options ) )
			}.getOrElse( Forbidden )
		}
	}
	
	def add() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				editCustomerForm.bindFromRequest.fold(
					formWithErrors => BadRequest( html.customerManagementCreate( user, formWithErrors, PromotionDevice.options, ProductType.options ) ),
					customer => {
						Customer.insert( customer )
						home.flashing( "success" -> "%s %s was added".format( customer.firstName, customer.lastName ) )
					}
				)
			}.getOrElse( Forbidden )
		}
	}
	
	def delete(id: Long) = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				Customer.findById(id) match {
					case Some( customer ) => {
						Customer.delete(id)
						home.flashing( "success" -> "%s %s was deleted".format(customer.firstName, customer.lastName ) )
					}
					case None => NotFound
				}
			}.getOrElse( Forbidden )
		}
	}
	
}