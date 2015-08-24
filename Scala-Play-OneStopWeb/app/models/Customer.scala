package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import scala.language.postfixOps

case class Customer( id: Option[Long], firstName: String, lastName: String, email: Option[String], phoneNumber: Option[String], promotionDeviceId: Long, favorites: Seq[Long] )

object Customer {
	
	/*
	val simpleParser = 
		get[Pk[Long]]( "Customers.CustomerID" ) ~
		get[String]( "Customers.FirstName" ) ~
		get[String]( "Customers.LastName" ) ~
		get[Option[String]]( "Customers.EmailAddress" ) ~
		get[Option[String]]( "Customers.PhoneNumber" ) ~ 
		get[Long]( "Customers.PromotionDeviceID" ) map {
			case id ~ firstName ~ lastName ~ email ~ phone ~ promotionDeviceId => Customer( id, firstName, lastName, email, phone, promotionDeviceId )
		}
		
	val withPromotionDevice = 
		Customer.simpleParser ~ PromotionDevice.simpleParser map {
			case customer ~ promotionDevice => ( customer, promotionDevice )
		}
		*/
	/*
	
	def findAll(): Seq[(Customer,PromotionDevice)] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					SELECT * FROM Customers 
					JOIN PromotionDevices ON Customers.PromotionDeviceID=PromotionDevices.PromotionDeviceID
				"""
			).as( Customer.withPromotionDevice *)
		}
	}*/
	
	def findAll(): Seq[(Customer,PromotionDevice)] = {
		val result = DB.withConnection{ implicit connection =>
			SQL(
				"""
					SELECT Customers.*, ProductType.ProductTypeID, PromotionDevices.* FROM Customers 
					LEFT JOIN CustomerFavorites ON Customers.CustomerID=CustomerFavorites.CustomerID 
					LEFT JOIN ProductType ON CustomerFavorites.ProductTypeID=ProductType.ProductTypeID 
					JOIN PromotionDevices ON Customers.PromotionDeviceID=PromotionDevices.PromotionDeviceID 
				"""	
			).as(Customer.withPromotionDevice *)
		}
		val groupedByFirstSix = result.groupBy( tuple => tuple._1 )
		val iter = groupedByFirstSix.map {
			case ( custWithFav, rest ) => {
				( Customer( custWithFav._1._1, custWithFav._1._2, custWithFav._1._3, custWithFav._1._4, custWithFav._1._5, custWithFav._1._6, rest.unzip._2.toSeq.foldLeft(Seq():Seq[Long])((acc,opt) => if (opt == None) acc else acc :+ opt.get) ), custWithFav._2 )
			}
		}
		iter.toSeq
	}
	
	/*
	def findAllAlt(): Seq[(Customer,PromotionDevice,List[String] )] = {
		val result = DB.withConnection { implicit connection => 
			SQL(
					
			).on
		}
	}*/
	
	/*
	def findById(id: Long): Option[Customer] = {
		DB.withConnection{ implicit connection => 
			SQL(
				"""
					SELECT * FROM Customers 
					WHERE CustomerID = {id}
				"""
			).on(
				'id -> id
			).as( Customer.simpleParser.singleOpt )
		}
	}*/
	
	
	val customerJoinFavorites = {
		get[Option[Long]]( "Customers.CustomerID" ) ~
		get[String]( "Customers.FirstName" ) ~
		get[String]( "Customers.LastName" ) ~
		get[Option[String]]( "Customers.EmailAddress" ) ~
		get[Option[String]]( "Customers.PhoneNumber" ) ~ 
		get[Long]( "Customers.PromotionDeviceID" ) ~ 
		get[Option[Long]]( "ProductType.ProductTypeID" ) map {
			case id ~ firstName ~ lastName ~ email ~ phone ~ promotionDeviceId ~ favorite => ( ( id, firstName, lastName, email, phone, promotionDeviceId ), favorite)
		}
	}
	
	val withPromotionDevice = 
	Customer.customerJoinFavorites ~ PromotionDevice.simpleParser map {
		case customerAndFavorite ~ promotionDevice => ( ( customerAndFavorite._1, promotionDevice ), customerAndFavorite._2 )
	}
	
	val favoritesMapper = {
		get[Long]( "CustomerFavorites.ProductTypeID" ) ~
		get[Long]( "CustomerFavorites.CustomerID" ) map {
			case productTypeId ~ customerId => ( productTypeId, customerId )
		}
	}
	
	def findById(id: Long): Option[(Customer)] = {
		val result = DB.withConnection{ implicit connection =>
			SQL(
				"""
					SELECT Customers.*, ProductType.ProductTypeID FROM Customers 
					LEFT JOIN CustomerFavorites ON Customers.CustomerID=CustomerFavorites.CustomerID 
					LEFT JOIN ProductType ON CustomerFavorites.ProductTypeID=ProductType.ProductTypeID 
					WHERE Customers.CustomerID = {id} 
				"""
			).on(
				'id -> id	
			).as(Customer.customerJoinFavorites *)
		}
		val groupedByFirstSix = result.groupBy( tuple => tuple._1 )
		val iter = groupedByFirstSix.map {
			case ( firstSix, rest ) => {
				Customer( firstSix._1, firstSix._2, firstSix._3, firstSix._4, firstSix._5, firstSix._6, rest.unzip._2.toSeq.foldLeft(Seq():Seq[Long])((acc,opt) => if (opt == None) acc else acc :+ opt.get) )
			}
		}
		iter.headOption
	}
	
	def update( id: Long, customer: Customer ) = {
		DB.withConnection { implicit connection => {
			// Update the main customer table
			SQL(
				"""
					UPDATE Customers
					SET FirstName = {firstname}, LastName = {lastname}, PhoneNumber = {phonenumber}, EmailAddress = {email}, PromotionDeviceID = {promotiondeviceid}
					WHERE CustomerID = {id}
				"""
			).on(
				'id -> id,
				'firstname -> customer.firstName,
				'lastname -> customer.lastName,
				'phonenumber -> customer.phoneNumber,
				'email -> customer.email,
				'promotiondeviceid -> customer.promotionDeviceId
			).executeUpdate()
			
			// Update the CustomerFavorites junction table
			val previousFavorites = SQL(
				"""
					SELECT ProductTypeID FROM CustomerFavorites
					WHERE CustomerID = {id}
				"""
			).on(
				'id -> id
			).as( scalar[Long] * ).toSet
			
			val updatedFavorites = customer.favorites.toSet
			
			// Add records for this user's new favorites
			val favoritesToAdd = updatedFavorites.diff( previousFavorites )
			favoritesToAdd.foreach( favorite => 
				SQL(
					"""
						INSERT INTO CustomerFavorites
						(CustomerID, ProductTypeID)
						VALUES
						({id}, {favorite})
					"""
				).on(
					'id -> id,
					'favorite -> favorite
				).executeUpdate()
			)
			
			// Delete records of favorites that no longer exist for this user
			val favoritesToRemove = previousFavorites.diff( updatedFavorites )
			favoritesToRemove.foreach( favorite =>
				SQL(
					"""
						DELETE FROM CustomerFavorites
						WHERE CustomerID = {id}
						AND ProductTypeID = {favorite}
					"""
				).on(
					'id -> id,
					'favorite -> favorite
				).executeUpdate()
			)
			
		}}
	}
	
	
	def insert( customer: Customer ) = {
		DB.withConnection{ implicit connection => {
			val customerId = SQL(
				"""
					INSERT INTO Customers
					(FirstName, LastName, PhoneNumber, EmailAddress, PromotionDeviceID)
					VALUES
					({firstname}, {lastname}, {phone}, {email}, {promotiondeviceid})
				"""
			).on(
				'firstname -> customer.firstName,
				'lastname -> customer.lastName,
				'phone -> customer.phoneNumber,
				'email -> customer.email,
				'promotiondeviceid -> customer.promotionDeviceId
			).executeInsert( scalar[Option[Long]] single )
			
			val favorites = customer.favorites.toSet
			
			favorites.foreach( productType => 
				SQL(
					"""
						INSERT INTO CustomerFavorites
						(CustomerID, ProductTypeID)
						VALUES
						({customerid}, {producttypeid})
					"""
				).on(
					'customerid -> customerId.get,
					'producttypeid -> productType
				).executeInsert()
			)
			
		}}
	}
	
	def delete( id: Long ) = {
		DB.withConnection{ implicit connection => 
			SQL(
				"""
					DELETE FROM Customers 
					WHERE CustomerID = {id}
				"""
			).on(
				'id -> id
			).executeUpdate()
		}
	}
	
	val nameMapper = {
		get[Option[Long]]("Customers.CustomerID") ~
		get[String]("Customers.FirstName") ~
		get[String]("Customers.LastName") map {
			case id~firstname~lastname => ( id.toString(), firstname + " " + lastname )
		}
	}
	
	def options(): Seq[(String, String)] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					SELECT CustomerID, FirstName, LastName FROM Customers
					ORDER BY FirstName ASC, LastName ASC
				"""
			).as( nameMapper * )
		}
	}
	
	
	def receiveEmailPromotion( productTypeId: Long ): Seq[String] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					SELECT EmailAddress FROM Customers
					JOIN CustomerFavorites ON Customers.CustomerID=CustomerFavorites.CustomerID
					JOIN PromotionDevices ON Customers.PromotionDeviceID=PromotionDevices.PromotionDeviceID
					WHERE CustomerFavorites.ProductTypeID={productTypeId}
					AND EmailAddress is not null
					AND ( PromotionDevices.Name = 'Email' OR PromotionDevices.Name = 'Phone and Email' )
				"""
			).on( 
				'productTypeId -> productTypeId
			).as( scalar[String] * )
		}
	}
	
	def receiveSmsPromotion( productTypeId: Long ): Seq[String] = {
		DB.withConnection { implicit connection =>
			SQL(
				"""
					SELECT PhoneNumber FROM Customers
					JOIN CustomerFavorites ON Customers.CustomerID=CustomerFavorites.CustomerID
					JOIN PromotionDevices ON Customers.PromotionDeviceID=PromotionDevices.PromotionDeviceID
					WHERE CustomerFavorites.ProductTypeID={productTypeId}
					AND PhoneNumber is not null
					AND ( PromotionDevices.Name = 'Phone' OR PromotionDevices.Name = 'Phone and Email' )
				"""
			).on( 
				'productTypeId -> productTypeId
			).as( scalar[String] * )
		}
	}
	
	
}