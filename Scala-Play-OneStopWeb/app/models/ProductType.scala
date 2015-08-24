package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class ProductType( id: Option[Long] = None, name: String )

object ProductType {

	val simpleParser = {
		get[Option[Long]]( "ProductType.ProductTypeID" ) ~
			get[String]( "ProductType.Name" ) map {
				case id ~ name => ProductType( id, name )
			}
	}

	def findById( id: Long ): Option[ProductType] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					SELECT * FROM ProductType 
					WHERE ProductTypeID = {id}
				""" 
			).on(
				'id -> id 
			).as( ProductType.simpleParser.singleOpt )
		}
	}

	def findByName( name: String ): Option[ProductType] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					SELECT * FROM ProductType 
					WHERE Name = {name}
				""" 
			).on(
				'name -> name 
			).as( ProductType.simpleParser.singleOpt )
		}
	}

	def findAll(): Seq[ProductType] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					SELECT * from ProductType
				""" 
			).as( ProductType.simpleParser * )
		}
	}

	def create( productType: ProductType ): Int = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					INSERT INTO ProductType 
					(Name) VALUES 
					({name})
				""" 
			).on(
				'name -> productType.name 
			).executeUpdate()
		}
	}

	def update( id: Long, productType: ProductType ): Int = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					UPDATE ProductType
					SET Name = {name} 
					WHERE ProductTypeID = {id}
				""" 
			).on(
				'id -> id,
				'name -> productType.name 
			).executeUpdate()
		}
	}

	def delete( id: Long ): Int = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					DELETE FROM ProductType 
					WHERE ProductTypeID = {id}
				""" 
			).on(
				'id -> id 
			).executeUpdate()
		}
	}

	/**
	 * Construct the (id,productType) mapping needed to fill a select options set.
	 */
	def options: Seq[( String, String )] = DB.withConnection { implicit connection =>
		SQL( 
			"""
				SELECT * FROM ProductType ORDER BY Name
			""" 
		).as( ProductType.simpleParser * ).map( pt => ( pt.id.toString , pt.name ) )
	}

}