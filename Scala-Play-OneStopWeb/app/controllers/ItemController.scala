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

object ItemController extends Controller with Secured {

	val home = Redirect( routes.ItemController.list() )
	
	val itemEditForm = Form(
		mapping(
			"id" -> optional(longNumber),
			"productType" -> longNumber,
			"sku" -> nonEmptyText,
			"description" -> nonEmptyText,
			"unitPrice" -> bigDecimal,
			"quantityOnHand" -> longNumber,
			"quantityPerOrder" -> longNumber,
			"quantityLowPoint" -> longNumber			
		)(Item.apply)(Item.unapply)
	)
	
	val itemCreateForm = Form(
		mapping(
			"id" -> optional(longNumber),
			"productType" -> longNumber,
			"sku" -> nonEmptyText.verifying( uniqueSkuConstraint ),
			"description" -> nonEmptyText,
			"unitPrice" -> bigDecimal,
			"quantityOnHand" -> longNumber,
			"quantityPerOrder" -> longNumber,
			"quantityLowPoint" -> longNumber			
		)(Item.apply)(Item.unapply)
	)
	
	lazy val uniqueSkuConstraint = Constraint[String]( Some("Unique"), "")( checkUniqueSku )
	
	def checkUniqueSku( sku: String ): ValidationResult = {
		Item.findBySku( sku ) match {
			case Some( item ) => Invalid( "An item with SKU: " + sku + " already exists in the database." )
			case None => Valid
		}
	}
	
	def list() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail( username ).map { user => 
				Ok( html.itemList(
						user,
						Item.findAll ) )
			}.getOrElse( Forbidden )
		}
	}
	
	def edit(id: Long) = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail( username ).map { user => 
				Item.findById(id) match {
					case Some(item) => {
						Ok( html.itemEdit(
								user,
								id,
								itemEditForm.fill(item),
								ProductType.options))
					}
					case None => NotFound
				}
			}.getOrElse( Forbidden )
		}
	}
	
	def update(id: Long) = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail( username ).map { user => 
				itemEditForm.bindFromRequest.fold ( 
					formWithErrors => {
						BadRequest( html.itemEdit( user, id, formWithErrors, ProductType.options ) )
					},
					itemToUpdate => {
						Item.update( id, itemToUpdate )
						home.flashing( "success" -> "%s has been updated".format( itemToUpdate.sku ) )
					}
				)
			}.getOrElse( Forbidden )
		}
	}
	
	def create() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail( username ).map { user => 
				Ok( html.itemCreate(
						user,
						itemCreateForm,
						ProductType.options))
			}.getOrElse( Forbidden )
		}
	}
	
	def add() = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail( username ).map { user => 
				itemCreateForm.bindFromRequest.fold (
					formWithErrors => BadRequest( html.itemCreate( user, formWithErrors, ProductType.options ) ),
					itemToAdd => {
						Item.create( itemToAdd )
						home.flashing( "success" -> "%s has been created.".format( itemToAdd.sku ) )
					}
				)
			}.getOrElse( Forbidden )
		}
	}
	
	def delete(id: Long) = IsAuthenticated { username => 
		implicit request => {
			User.findByEmail( username ).map { user => 
				Item.findById(id) match {
					case Some( item ) => {
						Item.delete(id)
						home.flashing( "success" -> "%s was deleted.".format( item.sku ) )
					}
					case None => NotFound
				}
			}.getOrElse( Forbidden )
		}
	}
	
}