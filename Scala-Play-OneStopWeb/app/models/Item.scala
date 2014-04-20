package models

import anorm._
import anorm.SqlParser._
import play.api.db.DB
import play.api.Play.current

case class Item( 
		id: Pk[Long] = NotAssigned, 
		productTypeId: Long, 
		sku: String, 
		description: String, 
		unitPrice: java.math.BigDecimal, 
		quantityOnHand: Int, 
		quantityPerOrder: Int, 
		quantityLowPoint: Int )

object Item {

	
	
	val simpleParser = 
		get[Pk[Long]]( "Inventory.ItemID" ) ~
		get[Long]( "Inventory.ProductTypeID" ) ~
		get[String]( "Inventory.SKU" ) ~
		get[String]( "Inventory.Description" ) ~
		get[java.math.BigDecimal]( "Inventory.UnitPrice" ) ~
		get[Int]( "Inventory.QuantityOnHand" ) ~
		get[Int]( "Inventory.QuantityPerOrder" ) ~
		get[Int]( "Inventory.QuantityLowPoint" ) map {
			case id ~ productTypeId ~ sku ~ description ~ unitPrice ~ quantityOnHand ~ quantityPerOrder ~ quantityLowPoint => Item( id, productTypeId, sku, description, unitPrice, quantityOnHand, quantityPerOrder, quantityLowPoint ) 
		}
	
	val withProductType = Item.simpleParser ~ ProductType.simpleParser map {
		case item ~ productType => ( item, productType )
	}
		
		
		
	
	def findById(id: Long): Option[(Item, ProductType)] = {
		None
	}
	
	def findAll(): Seq[(Item, ProductType)] = {
		DB.withConnection { implicit connection => 
			SQL(
				"""
					SELECT * FROM Inventory 
					JOIN ProductType ON Inventory.ProductTypeID = ProductType.ProductTypeID
				"""
			).as(Item.withProductType *)
		}
	}
	
	def create( item: Item ): Int = {
		0
	}
	
	def update( id: Long, item: Item ): Int = {
		0
	}
		
	def delete( id: Long ): Int = {
		0
	}
	
	
}