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
		unitPrice: BigDecimal, 
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
		
	
	def findById(id: Long): Option[Item] = {
		DB.withConnection { implicit connection => 
			SQL(
				"""
					SELECT * FROM Inventory 
					WHERE ItemID = {id}
				"""
			).on(
				'id -> id
			).as(Item.simpleParser.singleOpt)
		}
	}
	
	def findBySku( sku: String ): Option[Item] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					SELECT * FROM Inventory 
					WHERE SKU = {sku}
				"""
			).on(
				'sku -> sku
			).as( Item.simpleParser.singleOpt )
		}
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
		DB.withConnection { implicit connection => 
			SQL(
				"""
					INSERT INTO Inventory 
					(ProductTypeID, SKU, Description, UnitPrice, QuantityOnHand, QuantityPerOrder, QuantityLowPoint) VALUES
					({productTypeId}, {sku}, {description}, {unitPrice}, {quantityOnHand}, {quantityPerOrder}, {quantityLowPoint})
				"""
			).on(
				'productTypeId -> item.productTypeId,
				'sku -> item.sku,
				'description -> item.description,
				'unitPrice -> item.unitPrice.toString,
				'quantityOnHand -> item.quantityOnHand,
				'quantityPerOrder -> item.quantityPerOrder,
				'quantityLowPoint -> item.quantityLowPoint
			).executeUpdate()
		}
	}
	
	def update( id: Long, item: Item ): Int = {
		println( "Updating id: " + id + " Item: " + item)
		val rowsUpdated = DB.withConnection { implicit connection =>
			SQL(
				"""
					UPDATE Inventory 
					SET ProductTypeID = {productTypeId}, SKU = {sku}, Description = {description}, UnitPrice = {unitPrice}, QuantityOnHand = {quantityOnHand}, QuantityPerOrder = {quantityPerOrder}, QuantityLowPoint = {quantityLowPoint} 
					WHERE ItemID = {id}
				"""
			).on(
				'id -> id,
				'productTypeId -> item.productTypeId,
				'sku -> item.sku,
				'description -> item.description,
				'unitPrice -> item.unitPrice.toString,
				'quantityOnHand -> item.quantityOnHand,
				'quantityPerOrder -> item.quantityPerOrder,
				'quantityLowPoint -> item.quantityLowPoint
			).executeUpdate()
		}
		println( "Updated " + rowsUpdated + " rows" )
		rowsUpdated
	}
		
	def delete( id: Long ): Int = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					DELETE FROM Inventory 
					WHERE ItemID = {id}
				"""
			).on(
				'id -> id
			).executeUpdate()
		}
	}
	
	val nameMapper = {
		get[Pk[Long]]( "Inventory.ItemID" ) ~
		get[String]( "Inventory.SKU" ) ~
		get[String]( "Inventory.Description" ) map {
			case id~sku~description => ( id.toString, sku + " " + description )
		}
	}
	
	def options(): Seq[(String, String)] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					SELECT ItemID, SKU, Description FROM Inventory
				"""
			).as( nameMapper * )
		}
	}
	
	
}