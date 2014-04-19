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

object AdminUsers extends Controller with Secured {

	/**
	 * This result redirects to home.
	 */
	val Home = Redirect( routes.AdminUsers.list() )

	/**
	 * The user form for the edit screen, where password isn't mandatory
	 */
	val userEditForm = Form(
		mapping(
			"id" -> ignored( NotAssigned: Pk[Long] ),
			"email" -> email,
			"name" -> nonEmptyText,
			"password" -> default( text, "" ) )( User.apply )( User.unapply ).verifying( uniqueEmailIdConstraint ) )

	lazy val uniqueEmailIdConstraint = Constraint[User]( Some( "Unique Email-ID" ), "" )( checkUniqueEmailId )

	// Check to see if a user with a different ID has this email. 
	def checkUniqueEmailId( user: User ): ValidationResult = {
		User.findByEmail( user.email ) match {
			case Some( matchingUser ) => {
				// This email exists in the DB. Check to see if it's from the same user, or from a different user
				if ( matchingUser.id == user.id ) {
					Valid
				} else {
					Invalid( "Another user already has this email address" )
				}
			}
			case None => Valid // No one else has this email
		}
	}

	/**
	 * The user form for the create screen, where the password IS mandatory.
	 */
	val userCreateForm = Form(
		mapping(
			"id" -> ignored( NotAssigned: Pk[Long] ),
			"email" -> email.verifying( uniqueEmailConstraint ),
			"name" -> nonEmptyText,
			"password" -> nonEmptyText )( User.apply )( User.unapply ) )

	lazy val uniqueEmailConstraint = Constraint[String]( Some( "Unique" ), "" )( checkUniqueEmail )

	def checkUniqueEmail( email: String ): ValidationResult = {
		User.findByEmail( email ) match {
			case Some( user ) => Invalid( "A user with email: " + email + " already exists in the database" )
			case None => Valid
		}
	}

	def listOld( page: Int, orderBy: Int, filter: String ) = IsAuthenticated { username =>
		implicit request =>
			User.findByEmail( username ).map { user =>
				Ok( html.adminUserListOld(
					user,
					User.list( page, 5, orderBy, "%" + filter + "%" ),
					orderBy,
					filter ) )
			}.getOrElse( Forbidden )
	}

	def list() = IsAuthenticated { username =>
		implicit request =>
			User.findByEmail( username ).map { user =>
				Ok( html.adminUserList(
					user,
					User.list( pageSize = 10000 ) ) )
			}.getOrElse( Forbidden )
	}

	/**
	 * Display tbe 'create user' form
	 */
	def create = IsAuthenticated {
		username =>
			implicit request =>
				User.findByEmail( username ).map { user =>
					Ok( html.adminUserCreate( user, userCreateForm ) )
				}.getOrElse( Forbidden )
	}

	/**
	 * Handle the 'create user' form submission
	 */
	def add = IsAuthenticated {
		username =>
			implicit request =>
				User.findByEmail( username ).map { user =>
					userCreateForm.bindFromRequest.fold(
						formWithErrors => BadRequest( html.adminUserCreate( user, formWithErrors ) ),
						userToAdd => {
							User.create( userToAdd )
							Home.flashing( "success" -> "User %s has been created".format( userToAdd.name ) )
						} )
				}.getOrElse( Forbidden )
	}

	/**
	 * Dislay the 'edit user' form
	 *
	 * @param id Id of the user to edit
	 */
	def edit( id: Long ) = IsAuthenticated {
		username =>
			implicit request =>
				User.findByEmail( username ).map { user =>
					User.findById( id ) match {
						case Some( userToEdit ) => {
							Ok( html.adminUserEdit(
								user,
								id,
								userEditForm.fill( userToEdit ) ) )
						}
						case None => {
							NotFound
						}
					}
				}.getOrElse( Forbidden )
	}

	/**
	 * Handle the 'edit user' form submission
	 *
	 * @param id Id of the user to edit
	 */
	def update( id: Long ) = IsAuthenticated {
		username =>
			implicit request =>
				User.findByEmail( username ).map { user =>
					userEditForm.bindFromRequest.fold(
						formWithErrors => {
							Logger.debug( formWithErrors.toString )
							BadRequest( html.adminUserEdit( user, id, formWithErrors ) )
						},
						userToUpdate => {
							User.update( id, userToUpdate )
							Home.flashing( "success" -> "User %s has been updated".format( userToUpdate.name ) )
						} )
				}.getOrElse( Forbidden )
	}

	/**
	 * Handle user deletion.
	 */
	def delete( id: Long ) = IsAuthenticated {
		username =>
			implicit request =>
				User.findByEmail( username ).map { user =>
					val userToDelete = User.findById( id )
					if ( userToDelete.get.email == user.email ) {
						Home.flashing( "error" -> "Can't delete your own account!" )
					} else {
						User.delete( id )
						Home.flashing( "success" -> "User has been deleted" )
					}

				}.getOrElse( Forbidden )
	}

}