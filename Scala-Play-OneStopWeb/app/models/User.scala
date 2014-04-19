package models

import play.api.db._
import play.api.Play.current

import play.api.Logger

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

import org.mindrot.jbcrypt.BCrypt

case class User( id: Pk[Long] = NotAssigned, email: String, name: String, password: String )

object User {

	// -- Parsers

	/**
	 * Parse a User from a ResultSet
	 */
	val simple = {
		get[Pk[Long]]( "user.id" ) ~
			get[String]( "user.email" ) ~
			get[String]( "user.name" ) ~
			get[String]( "user.password" ) map {
				case id ~ email ~ name ~ password => User( id, email, name, password )
			}
	}

	val withoutPassword = {
		get[Pk[Long]]( "user.id" ) ~
			get[String]( "user.email" ) ~
			get[String]( "user.name" ) map {
				case id ~ email ~ name => User( id, email, name, "" )
			}
	}

	// -- Queries

	/**
	 * Retrieve a User from email.
	 */
	def findByEmail( email: String ): Option[User] = {
		DB.withConnection { implicit connection =>
			SQL( "select * from user where email = {email}" ).on(
				'email -> email ).as( User.simple.singleOpt )
		}
	}

	/**
	 * Retrieve a User from email.
	 */
	def findById( id: Long ): Option[User] = {
		DB.withConnection { implicit connection =>
			SQL( "SELECT id, name, email FROM user WHERE id = {id}" ).on(
				'id -> id ).as( User.withoutPassword.singleOpt )
		}
	}

	/**
	 * Retrieve all users.
	 */
	def findAll: Seq[User] = {
		DB.withConnection { implicit connection =>
			SQL( "SELECT * FROM user" ).as( User.simple * )
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
					SELECT * FROM user 
					WHERE user.name LIKE {filter} 
					ORDER BY {orderBy} nulls last 
					LIMIT {pageSize} OFFSET {offset}
				""" ).on(
					'pageSize -> pageSize,
					'offset -> offest,
					'filter -> filter,
					'orderBy -> orderBy ).as( User.simple * )

			val totalRows = SQL(
				"""
					SELECT count(*) FROM user 
					WHERE user.name LIKE {filter}
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
						SELECT * FROM user  
						WHERE email = {email}
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
	def create( user: User ): User = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					INSERT INTO user 
					(email, name, password) 
					VALUES ({email}, {name}, {password})
				""" ).on(
					'email -> user.email,
					'name -> user.name,
					'password -> BCrypt.hashpw( user.password, BCrypt.gensalt() ) ).executeUpdate()

			user

		}
	}

	/**
	 * Delete a user.
	 *
	 * @param id Id of the user to delete.
	 */
	def delete( id: Long ) = {
		DB.withConnection { implicit connection =>
			SQL( "DELETE FROM user WHERE id = {id}" ).on( 'id -> id ).executeUpdate()
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
						UPDATE user
						SET name = {name}, email = {email}
						WHERE id = {id}
					""" ).on(
						'id -> id,
						'name -> user.name,
						'email -> user.email ).executeUpdate()
			} else {
				SQL(
					"""
						UPDATE user
						SET name = {name}, email = {email}, password = {password}
						WHERE id = {id}
					""" ).on(
						'id -> id,
						'name -> user.name,
						'email -> user.email,
						'password -> BCrypt.hashpw( user.password, BCrypt.gensalt() ) ).executeUpdate()
			}
		}
	}

}
