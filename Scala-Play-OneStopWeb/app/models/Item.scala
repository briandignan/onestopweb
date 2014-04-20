package models

import anorm._

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

	def findById(id: Long): Option[Item] = {
		None
	}
	
	def findAll(): Seq[Item] = {
		Seq()
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