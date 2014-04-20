package models

import anorm._
import anorm.SqlParser._

case class ProductType( id: Pk[Long] = NotAssigned, name: String )

object ProductType {

	val simpleParser = {
		get[Pk[Long]]( "ProductType.ProductTypeID" ) ~
		get[String]( "ProductType.Name" ) map {
			case id ~ name => ProductType( id, name )
		}
	}
	
	def findById( id: Long ): Option[ProductType] = {
		None
	}
	
	def findByName( name: String ): Option[ProductType] = {
		None
	}
	
	def findAll(): Seq[ProductType] = {
		Seq()
	}
	
	def create( productType: ProductType ): Int = {
		0
	}
	
	def update( id: Long, productType: ProductType ): Int = {
		0
	}
	
	def delete( id: Long ): Int = {
		0
	}
	
}