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

object ProductTypeController extends Controller with Secured {

	/**
	 * This result redirects to home.
	 */
	val Home = Redirect( routes.ProductTypeController.list() )
	
	/**
	 * The product type form for the create or edit screens.
	 */
	val productTypeForm = Form(
		mapping(
			"id" -> ignored( NotAssigned: Pk[Long] ),
			"name" -> nonEmptyText.verifying( uniqueNameConstraint )
			)( ProductType.apply )( ProductType.unapply ) )

	lazy val uniqueNameConstraint = Constraint[String]( Some( "Unique" ), "" )( checkUniqueName )

	private def checkUniqueName( name: String ): ValidationResult = {
		ProductType.findByName( name ) match {
			case Some( productType ) => Invalid( "A department with name: " + name + " already exists in the database" )
			case None => Valid
		}
	}
	
	/**
	 * Displays the list of departments in the database
	 */
	def list() = IsAuthenticated { username =>
		implicit request =>
			User.findByEmail( username ).map { user =>
				Ok( html.productTypeList(
						user,
						ProductType.findAll ) )
			}.getOrElse( Forbidden )
	}
	
	/**
	 * Displayed the 'edit product-type' form
	 */
	def edit(id: Long) = IsAuthenticated { username => 
		implicit request =>
			User.findByEmail( username ).map { user =>
				ProductType.findById(id) match {
					case Some(productType) => {
						Ok( html.productTypeEdit(
							user,
							productType.id.get,
							productTypeForm.fill(productType) ) )
					}
					case None => NotFound
				}
			}.getOrElse( Forbidden )
	}
		
	/**
	 * Handles the 'edit product-type' form
	 */
	def update(id: Long) = IsAuthenticated { username =>
		implicit request =>
			User.findByEmail( username ).map { user =>
				productTypeForm.bindFromRequest.fold(
					formWithErrors => {
						BadRequest( html.productTypeEdit(user, id, formWithErrors))
					},
					productType => {
						ProductType.update(id, productType)
						Home.flashing( "success" -> "%s has been updated".format( productType.name ) )
					}
				)
			}.getOrElse( Forbidden )
	}
	
	/**
	 * Displays the 'create product-type' form
	 */
	def create() = IsAuthenticated { username =>
		implicit request =>
			User.findByEmail( username ).map { user =>
				Ok( html.productTypeCreate(
						user,
						productTypeForm))
			}.getOrElse( Forbidden )
	}
	
	/**
	 * Handles the 'create product-type' form
	 */
	def add() = IsAuthenticated { username =>
		implicit request =>
			User.findByEmail( username ).map { user =>
				productTypeForm.bindFromRequest.fold (
					formWithErrors => {
						BadRequest( html.productTypeCreate( user, formWithErrors ) )
					},
					productType => {
						ProductType.create( productType )
						Home.flashing( "success" -> "%s was added".format( productType.name ) )
					}
				)
			}.getOrElse( Forbidden )
	}
	
	/**
	 * Deletes a product-type from the database
	 */
	def delete(id: Long) = IsAuthenticated { username =>
		implicit request =>
			User.findByEmail( username ).map { user =>
				val name = ProductType.findById(id).get.name
				ProductType.delete( id )
				Home.flashing( "success" -> "%s was deleted".format( name ) )
			}.getOrElse( Forbidden )
	}
	
}