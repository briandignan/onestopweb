package models

import play.api.db._
import play.api.Play.current

import play.api.Logger

import anorm._
import anorm.SqlParser._


import org.mindrot.jbcrypt.BCrypt

import scala.language.postfixOps

case class User( id: Option[Long] = None, email: String, firstName: String, lastName: String, password: String )

object User {

	// -- Parsers

	/**
	 * Parse a User from a ResultSet
	 */
	val simple = {
		get[Option[Long]]( "AdminUsers.UserID" ) ~
			get[String]( "AdminUsers.EmailAddress" ) ~
			get[String]( "AdminUsers.FirstName" ) ~
			get[String]( "AdminUsers.LastName" ) ~
			get[String]( "AdminUsers.Password" ) map {
				case id ~ email ~ firstName ~ lastName ~ password => User( id, email, firstName, lastName, password )
			}
	}

	/**
	 * Parse a User from a ResultSet that doesn't contain a password
	 */
	val withoutPassword = {
		get[Option[Long]]( "AdminUsers.UserID" ) ~
			get[String]( "AdminUsers.EmailAddress" ) ~
			get[String]( "AdminUsers.FirstName" ) ~
			get[String]( "AdminUsers.LastName" ) map {
				case id ~ email ~ firstName ~ lastName => User( id, email, firstName, lastName, "" )
			}
	}

	// -- Queries

	/**
	 * Retrieve a User from email.
	 */
	def findByEmail( email: String ): Option[User] = {
		DB.withConnection { implicit connection =>
			SQL( "SELECT * FROM AdminUsers WHERE EmailAddress = {email}" ).on(
				'email -> email ).as( User.simple.singleOpt )
		}
	}

	/**
	 * Retrieve a User from email.
	 */
	def findById( id: Long ): Option[User] = {
		DB.withConnection { implicit connection =>
			SQL( "SELECT UserID, FirstName, LastName, EmailAddress FROM AdminUsers WHERE UserID = {id}" ).on(
				'id -> id ).as( User.withoutPassword.singleOpt )
		}
	}

	/**
	 * Retrieve all users.
	 */
	def findAll: Seq[User] = {
		DB.withConnection { implicit connection =>
			SQL( "SELECT * FROM AdminUsers" ).as( User.simple * )
		}
	}

	/**
	 * Return a page of Users.
	 *
	 * @param page Page to display
	 * @param pageSize Number of computers per page
	 * @param orderBy User property used for sorting
	 * @param filter Filter applied on the name column
	 */
	def list( page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%" ): Page[User] = {

		val offest = pageSize * page

		DB.withConnection { implicit connection =>

			val users = SQL(
				"""
					SELECT * FROM AdminUsers 
					WHERE LastName LIKE {filter} 
					ORDER BY {orderBy} nulls last 
					LIMIT {pageSize} OFFSET {offset}
				""" ).on(
					'pageSize -> pageSize,
					'offset -> offest,
					'filter -> filter,
					'orderBy -> orderBy ).as( User.simple * )

			val totalRows = SQL(
				"""
					SELECT count(*) FROM AdminUsers 
					WHERE LastName LIKE {filter}
				""" ).on(
					'filter -> filter ).as( scalar[Long].single )

			Page( users, page, offest, totalRows )

		}

	}

	/**
	 * Authenticate a User.
	 */
	def authenticate( email: String, password: String ): Option[User] = {
		DB.withConnection { implicit connection =>
			{
				val results = SQL(
					"""
						SELECT * FROM AdminUsers  
						WHERE EmailAddress = {email}
					""" ).on( 'email -> email )
				val matchingUsers = results.as( User.simple * ).toList
				if ( matchingUsers.size != 1 ) {
					None
				} else {
					// Confirm that the PW hash matches
					val user = matchingUsers( 0 )
					if ( BCrypt.checkpw( password, user.password ) ) {
						// Confirmed that the password matches
						Some( user )
					} else {
						None
					}
				}
			}
		}
	}

	/**
	 * Create a User.
	 */
	def create( user: User ): Int = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					INSERT INTO AdminUsers 
					(EmailAddress, FirstName, LastName, Password) 
					VALUES ({email}, {firstName}, {lastName}, {password})
				""" ).on(
					'email -> user.email,
					'firstName -> user.firstName,
					'lastName -> user.lastName,
					'password -> BCrypt.hashpw( user.password, BCrypt.gensalt() ) ).executeUpdate()
		}
	}

	/**
	 * Delete a user.
	 *
	 * @param id Id of the user to delete.
	 */
	def delete( id: Long ) = {
		DB.withConnection { implicit connection =>
			SQL( "DELETE FROM AdminUsers WHERE UserID = {id}" ).on( 'id -> id ).executeUpdate()
		}
	}

	/**
	 * Update a user.
	 *
	 * @param id The user id
	 * @param user The user values.
	 */
	def update( id: Long, user: User ) = {
		DB.withConnection { implicit connection =>
			if ( user.password == "" ) {
				SQL(
					"""
						UPDATE AdminUsers
						SET FirstName = {firstName}, LastName = {lastName}, EmailAddress = {email}
						WHERE UserID = {id}
					""" ).on(
						'id -> id,
						'firstName -> user.firstName,
						'lastName -> user.lastName,
						'email -> user.email ).executeUpdate()
			} else {
				SQL(
					"""
						UPDATE AdminUsers
						SET FirstName = {firstName}, LastName = {lastName}, EmailAddress = {email}, Password = {password}
						WHERE UserID = {id}
					""" ).on(
						'id -> id,
						'firstName -> user.firstName,
						'lastName -> user.lastName,
						'email -> user.email,
						'password -> BCrypt.hashpw( user.password, BCrypt.gensalt() ) ).executeUpdate()
			}
		}
	}

}
