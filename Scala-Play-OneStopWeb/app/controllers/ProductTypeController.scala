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
			"name" -> email.verifying( uniqueNameConstraint )
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
	
	def edit(id: Long) = IsAuthenticated { username => 
		implicit request =>
			User.findByEmail( username ).map { user =>
				Ok
			}.getOrElse( Forbidden )
	}
	
	def create() = IsAuthenticated { username =>
		implicit request =>
			User.findByEmail( username ).map { user =>
				Ok
			}.getOrElse( Forbidden )
	}
	
	def add() = IsAuthenticated { username =>
		implicit request =>
			User.findByEmail( username ).map { user =>
				Ok
			}.getOrElse( Forbidden )
	}
	
	def update(id: Long) = IsAuthenticated { username =>
		implicit request =>
			User.findByEmail( username ).map { user =>
				Ok
			}.getOrElse( Forbidden )
	}
	
	def delete(id: Long) = IsAuthenticated { username =>
		implicit request =>
			User.findByEmail( username ).map { user =>
				Ok
			}.getOrElse( Forbidden )
	}
	
}