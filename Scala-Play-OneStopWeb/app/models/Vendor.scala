package models

import anorm._

case class Vendor( 
		id: Pk[Long], 
		name: String, 
		city: Option[String], 
		state: Option[String], 
		country: Option[String], 
		email: Option[String], 
		phoneOne: Option[String], 
		phoneTwo: Option[String], 
		fax: Option[String] )

object Vendor {

	def findById( id: Long ): Option[Vendor] = {
		None
	}
	
	def findAll(): Seq[Vendor] = {
		Seq()
	}
	
	def create( vendor: Vendor ): Int = {
		0
	}
	
	def update( id: Long, vendor: Vendor ): Int = {
		0
	}
	
	def delete( id: Long ): Int = {
		0
	}
	
}