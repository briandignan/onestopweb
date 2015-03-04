package models

import java.util.Date

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class CustomerOrder( id: Pk[Long], customerId: Option[Long], dateTime: Date, items: Seq[CustomerOrderItem] )

object CustomerOrder {

	
	def create( order: CustomerOrder ) = {
		DB.withConnection { implicit connection => 
			val orderId = SQL(
				"""
					INSERT INTO CustomerOrders
					(CustomerID, DateTime)
					VALUES
					({customerId}, {dateTime})
				"""
			).on(
				'customerId -> order.customerId,
				'dateTime -> order.dateTime
			).executeInsert( scalar[Pk[Long]].single )
			
			println( "Inserted order. CustomerOrderID: " + orderId )
			println( "Number of items: " + order.items.size )
			
			order.items.foreach( item => {
					val rowsInserted = SQL(
						"""
							INSERT INTO CustomerOrderItems
							(CustomerOrderID, ItemID, QuantityOrdered, UnitPrice)
							VALUES
							({customerOrderId}, {itemId}, {quantity}, {unitPrice})
						"""
					).on(
						'customerOrderId -> orderId.get,
						'itemId -> item.itemId,
						'quantity -> item.quantity,
						'unitPrice -> item.unitPrice.toString
					).executeUpdate()
					println( "Inserted " + rowsInserted + " to CustomerOrderItems. CustomerOrderID: " + orderId + " ItemID: " + item.itemId )
				}
			)
		}
	}
	
	val joinMapper = {
		get[Pk[Long]]( "CustomerOrders.CustomerOrderID" ) ~
		get[Option[Long]]( "CustomerOrders.CustomerID" ) ~
		get[Date]( "CustomerOrders.DateTime" ) ~
		get[Long]( "CustomerOrderItems.ItemID" ) ~
		get[Long]( "CustomerOrderItems.QuantityOrdered" ) ~
		get[java.math.BigDecimal]( "CustomerOrderItems.UnitPrice" ) map {
			case id~customerId~dateTime~itemId~quantity~unitPrice => ( ( id, customerId, dateTime ), CustomerOrderItem( id, itemId, quantity, unitPrice ) )
		}
	}
	
	def list(): Seq[CustomerOrder] = {
		val orders = DB.withConnection { implicit connection => 
			val result = SQL(
				"""
					SELECT * from CustomerOrders 
					JOIN CustomerOrderItems ON CustomerOrders.CustomerOrderID=CustomerOrderItems.CustomerOrderID
				"""
			).as( joinMapper * )
			val groupedByOrder = result.groupBy( rows => rows._1 )
			groupedByOrder.map{ 
				case ( partialOrder, rest ) => {
					val items = rest.unzip._2
					CustomerOrder( partialOrder._1, partialOrder._2, partialOrder._3, items )
				}
			}.toSeq
		}
		orders
	}
	
	def findById(id: Long): Option[CustomerOrder] = {
		DB.withConnection { implicit connection =>
			val result = SQL( 
				"""
					SELECT * from CustomerOrders
					JOIN CustomerOrderItems ON CustomerOrders.CustomerOrderID=CustomerOrderItems.CustomerOrderID
					WHERE CustomerOrders.CustomerOrderID = {id}
				"""
			).on(
				'id -> id
			).as( joinMapper * )
			val groupedByOrder = result.groupBy( rows => rows._1 )
			val iter = groupedByOrder.map {
				case ( partialOrder, rest ) => {
					val items = rest.unzip._2
					CustomerOrder( partialOrder._1, partialOrder._2, partialOrder._3, items )
				}
			}
			iter.headOption
		}
	}
	
}