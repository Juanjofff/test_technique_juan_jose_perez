/*==============================================================*/
/* DBMS name:      PostgreSQL 8                                 */
/* Created on:     29/11/2025 10:00:00                            */
/*==============================================================*/

create sequence SEQ_CLIENTE
increment 1
minvalue 1
start 1;

create sequence SEQ_CUENTA
increment 1
minvalue 1
start 1;

create sequence SEQ_MOVIMIENTOS
increment 1
minvalue 1
start 1;

-- TABLES --

/*==============================================================*/
/* Table: CLIENTE                                               */
/*==============================================================*/
create table CLIENTE (
   ID_CLIENTE            BIGINT                 not null default nextval('SEQ_CLIENTE'),
   NOMBRE                varchar(300)           not null,
   GENERO                varchar(50)            not null,
   IDENTIFICACION        varchar(50)            not null,
   DIRECCION             varchar(300)           not null,
   TELEFONO              varchar(20)            not null,
   CONTRASENA            varchar(100)           not null,
   ESTADO                varchar(50)            not null,
   constraint PK_CLIENTE primary key (ID_CLIENTE)
);

/*==============================================================*/
/* Table: CUENTA                                               */
/*==============================================================*/
create table CUENTA (
   ID_CUENTA            BIGINT              not null default nextval('SEQ_CUENTA'),
   ID_CLIENTE           BIGINT              not null,
   NUMERO_CUENTA        varchar(50)         not null,
TIPO_CUENTA             varchar(50)         not null,
SALDO_INICIAL           numeric(18,2)       not null,
ESTADO                  varchar(50)         not null,
   constraint PK_CUENTA primary key (ID_CUENTA)
);

alter table CUENTA add constraint FK_CUENTA_CLIENTE foreign key (ID_CLIENTE)
      references CLIENTE (ID_CLIENTE) on delete restrict on update restrict;

/*==============================================================*/
/* Table: MOVIMIENTOS                                           */
/*==============================================================*/
create table MOVIMIENTOS (
   ID_MOVIMIENTO            BIGINT                  not null default nextval('SEQ_MOVIMIENTOS'),
   ID_CUENTA                BIGINT                  not null,
   TIPO_MOVIMIENTO          varchar(50)             not null,
   VALOR                    numeric(18,2)           not null,
   FECHA_MOVIMIENTO         timestamp               not null,
   SALDO                    numeric(18,2)           not null,
   constraint PK_MOVIMIENTOS primary key (ID_MOVIMIENTO)
);

alter table MOVIMIENTOS add constraint FK_MOVIMIENTOS_CUENTA foreign key (ID_CUENTA)
      references CUENTA (ID_CUENTA) on delete restrict on update restrict;

ALTER TABLE cuenta ADD CONSTRAINT cuenta_unique UNIQUE (numero_cuenta);

/*==============================================================*/
/* Table: CUSTOMER_REFERENCE                                    */
/*==============================================================*/
/* This table is used by account-service to maintain a denormalized */
/* copy of customer data for eventual consistency via Kafka events */
create table CUSTOMER_REFERENCE (
   ID_CLIENTE            BIGINT                 not null,
   NOMBRE                varchar(300)           not null,
   IDENTIFICACION        varchar(50)            not null,
   ESTADO                varchar(50)            not null,
   constraint PK_CUSTOMER_REFERENCE primary key (ID_CLIENTE)
);
