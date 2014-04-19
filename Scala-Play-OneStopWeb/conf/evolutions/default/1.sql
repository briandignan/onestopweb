# --- First database schema

# --- !Ups

CREATE TABLE AdminUsers (
  UserID INT UNSIGNED NOT NULL AUTO_INCREMENT,
  EmailAddress VARCHAR(255) NOT NULL,
  FirstName VARCHAR(255) NOT NULL,
  LastName VARCHAR(255) NOT NULL,
  Password VARCHAR(255) NOT NULL,
  PRIMARY KEY (UserID)
);

CREATE UNIQUE INDEX  EmailIndex ON AdminUsers (EmailAddress);


CREATE TABLE ProductType(
	ProductTypeID INT UNSIGNED NOT NULL AUTO_INCREMENT,
	Name VARCHAR(255) NOT NULL,
	PRIMARY KEY (ProductTypeID)
);


/* 
 * Doesn't contain UnitCost because the cost for a single item could vary from
 * vendor to vendor. 
 */
CREATE TABLE Inventory(
	ItemID INT UNSIGNED NOT NULL AUTO_INCREMENT,
	ProductTypeID INT UNSIGNED NOT NULL,
	SKU VARCHAR(255) NOT NULL,
	Description VARCHAR(255) NOT NULL,
	UnitPrice DECIMAL(10,2) NOT NULL,
	QuantityOnHand INT UNSIGNED NOT NULL,
	QuantityPerOrder INT UNSIGNED NOT NULL,
	QuantityLowPoint INT UNSIGNED NOT NULL, 
	PRIMARY KEY (ItemID)
);

ALTER TABLE Inventory ADD CONSTRAINT FK_Inventory_ProductType FOREIGN KEY (ProductTypeID) REFERENCES ProductType (ProductTypeID) ON DELETE RESTRICT ON UPDATE CASCADE;


CREATE TABLE Vendors(
	VendorID INT UNSIGNED NOT NULL AUTO_INCREMENT,
	Name VARCHAR(255) NOT NULL,
	City VARCHAR(255),
	State VARCHAR(255),
	Country VARCHAR(255),
	EmailAddress VARCHAR(255),
	PhoneOne VARCHAR(255),
	PhoneTwo VARCHAR(255),
	Fax VARCHAR(255),
	PRIMARY KEY (VendorID)
);


/* Contains which vendors offer which items for sale, and at what cost */
CREATE TABLE VendorItems ( 
	VendorID INT UNSIGNED NOT NULL,
	ItemID INT UNSIGNED NOT NULL,
	UnitCost DECIMAL(10,2) NOT NULL,
	PRIMARY KEY (VendorID, ItemID)
);

ALTER TABLE VendorItems ADD CONSTRAINT FK_VendorItems_Inventory FOREIGN KEY (ItemID) REFERENCES Inventory (ItemID) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE VendorItems ADD CONSTRAINT FK_VendorItems_Vendor FOREIGN KEY (VendorID) REFERENCES Vendors (VendorID) ON DELETE RESTRICT ON UPDATE CASCADE;


CREATE TABLE Customers(
	CustomerID INT UNSIGNED NOT NULL AUTO_INCREMENT,
	FirstName VARCHAR(255) NOT NULL,
	LastName VARCHAR(255) NOT NULL,
	PhoneNumber VARCHAR(255),
	EmailAddress VARCHAR(255),
	PromotionDevice TINYINT UNSIGNED NOT NULL DEFAULT 0, /* 0 if no promotion, 1 for phone, 2 for email */
	PRIMARY KEY (CustomerID)
);


CREATE TABLE CustomerOrders(
	CustomerOrderID INT UNSIGNED NOT NULL AUTO_INCREMENT,
	/* CustomerID is nullable because most customers will not be tracked by this system */
	CustomerID INT UNSIGNED,
	DateTime TIMESTAMP NOT NULL,
	PRIMARY KEY (CustomerOrderID)
);

ALTER TABLE CustomerOrders ADD CONSTRAINT FK_CustomerOrders_Customers FOREIGN KEY (CustomerID) REFERENCES Customers (CustomerID) ON DELETE RESTRICT ON UPDATE CASCADE;


CREATE TABLE CustomerOrderItems(
	CustomerOrderID INT UNSIGNED NOT NULL,
	ItemID INT UNSIGNED NOT NULL,
	QuantityOrdered INT UNSIGNED NOT NULL,
	/* Unit price exists in Items, but it's also here because pricing will vary over time
	 * and the pricing change in the Items table shouldn't impact previous sales. 
	 */
	UnitPrice DECIMAL(10,2) NOT NULL, 
	PRIMARY KEY (CustomerOrderID, ItemID)
);

ALTER TABLE CustomerOrderItems ADD CONSTRAINT FK_CustomerOrderItems_CustomerOrders FOREIGN KEY (CustomerOrderID) REFERENCES CustomerOrders (CustomerOrderID) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE CustomerOrderItems ADD CONSTRAINT FK_CustomerOrderItems_Inventory FOREIGN KEY (ItemID) REFERENCES Inventory (ItemID) ON DELETE RESTRICT ON UPDATE CASCADE;


CREATE TABLE CustomerFavorites(
	CustomerID INT UNSIGNED NOT NULL,
	ProductTypeID INT UNSIGNED NOT NULL,
	PRIMARY KEY (CustomerID, ProductTypeID)
);

ALTER TABLE CustomerFavorites ADD CONSTRAINT FK_CustomerFavorites_Customers FOREIGN KEY (CustomerID) REFERENCES Customers (CustomerID) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE CustomerFavorites ADD CONSTRAINT FK_CustomerFavorites_ProductType FOREIGN KEY (ProductTypeID) REFERENCES ProductType (ProductTypeID) ON DELETE RESTRICT ON UPDATE CASCADE;


CREATE TABLE VendorOrders(
	VendorOrderID INT UNSIGNED NOT NULL AUTO_INCREMENT,
	VendorID INT UNSIGNED NOT NULL,
	DateOrdered  TIMESTAMP NOT NULL,
	DateDelivered  TIMESTAMP,
	PRIMARY KEY (VendorOrderID)
);

ALTER TABLE VendorOrders ADD CONSTRAINT FK_VendorOrders_Vendor FOREIGN KEY (VendorID) REFERENCES Vendors (VendorID) ON DELETE RESTRICT ON UPDATE CASCADE;


CREATE TABLE VendorOrderItems(
	VendorOrderID INT UNSIGNED NOT NULL,
	ItemID INT UNSIGNED NOT NULL,
	QuantityOrdered INT UNSIGNED NOT NULL,
	UnitCost DECIMAL(10,2) NOT NULL,
	/* Unit cost exists in VendorItems, but it's also here because pricing will vary over 
	 * time and the pricing change in the VendorItems table shouldn't impact previous 
	 * purchases. 
	 */
	PRIMARY KEY (VendorOrderID, ItemID)
);

ALTER TABLE VendorOrderItems ADD CONSTRAINT FK_VendorOrdersItems_VendorOrders FOREIGN KEY (VendorOrderID) REFERENCES VendorOrders (VendorOrderID) ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE VendorOrderItems ADD CONSTRAINT FK_VendorOrdersItems_Inventory FOREIGN KEY (ItemID) REFERENCES Inventory (ItemID) ON DELETE RESTRICT ON UPDATE CASCADE;




# --- !Downs

drop table if exists AdminUsers;
drop table if exists ProductType;
drop table if exists Inventory;
drop table if exists Vendors;
drop table if exists VendorItems;
drop table if exists Customers;
drop table if exists CustomerItems;
drop table if exists CustomerOrderItems;
drop table if exists CustomerFavorites;
drop table if exists VendorOrders;
drop table if exists VendorOrderItems;

