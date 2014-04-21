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

object CustomerManagementController extends Controller with Secured {

	val editCustomerForm = Form(
		mapping(
			"id" -> ignored(NotAssigned:Pk[Long]),
			"firstName" -> nonEmptyText,
			"lastName" -> nonEmptyText,
			"email" -> optional(text),
			"phone" -> optional(text), //TODO Verify that it's a phone number
			"promotionDevice" -> longNumber
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
						Ok( html.customerManagementEdit( user, id, editCustomerForm.fill(customer), PromotionDevice.options ) )
					} 
					case None => NotFound
				}
			}.getOrElse( Forbidden )
		}
	}
	
	def update(id: Long) = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok
			}.getOrElse( Forbidden )
		}
	}
	
	def create() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok
			}.getOrElse( Forbidden )
		}
	}
	
	def add() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok
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