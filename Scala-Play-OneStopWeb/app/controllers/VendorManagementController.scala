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

object VendorManagementController extends Controller with Secured {

	val home = Redirect( routes.VendorManagementController.list )
	
	val vendorForm: Form[Vendor] = Form(
		mapping( 
			"id" -> optional(longNumber),
			"name" -> nonEmptyText,
			"city" -> optional(text),
			"state" -> optional(text),
			"country" -> optional(text),
			"emailAddress" -> optional(text),
			"phoneOne" -> optional(text),
			"phoneTwo" -> optional(text),
			"fax" -> optional(text),
			"items" -> seq(
					mapping(
						"itemId" -> longNumber,
						"unitCost" -> bigDecimal
					)(VendorItem.apply)(VendorItem.unapply)
			)
		)(Vendor.apply)(Vendor.unapply)
	)
	
	def list() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok( html.vendorManagementList( user, Vendor.findAll ) )
			}.getOrElse( Forbidden )
		}
	}
	
	def edit( id: Long ) = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				Vendor.findById(id).map { vendor => 
					Ok( html.vendorManagementEdit( user, id, vendorForm.fill(vendor), Item.options))
				}.getOrElse( NotFound )
			}.getOrElse( Forbidden )
		}
	}
	
	def update( id: Long ) = IsAuthenticated { username =>
		implicit request => {
			User.findByEmail(username).map { user =>
				vendorForm.bindFromRequest.fold(
					formWithErrors => BadRequest( html.vendorManagementEdit( user, id, formWithErrors, Item.options ) ),
					vendorToUpdate => {
						Vendor.update(id, vendorToUpdate)
						home.flashing( "success" -> "%s has been updated".format( vendorToUpdate.name ) )
					}
				)
			}.getOrElse( Forbidden )
		}
	}
	
	def create() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail(username).map { user =>
				Ok( html.vendorManagementCreate( user, vendorForm, Item.options ) )
			}.getOrElse( Forbidden )
		}
	}
	
	def add() = IsAuthenticated { username =>
		implicit request => {
			User.findByEmail(username).map {user =>
				vendorForm.bindFromRequest.fold(
					formWithErrors => BadRequest( html.vendorManagementCreate( user, formWithErrors, Item.options ) ),
					vendorToCreate => {
						Vendor.create(vendorToCreate)
						home.flashing( "success" -> "%s was created".format( vendorToCreate.name ) )
					}
				)
			}.getOrElse( Forbidden )
		}
	}
	
	def delete( id: Long ) = IsAuthenticated { username =>
		implicit request => {
			User.findByEmail(username).map { user =>
				Vendor.findById(id).map { vendor =>
					Vendor.delete(id)
					home.flashing( "success" -> "%s was deleted".format( vendor.name ) )
				}.getOrElse( NotFound )
			}.getOrElse( Forbidden )
		}
	}
	
}