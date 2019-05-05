# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table ad_photos (
  id                            bigint auto_increment not null,
  path                          varchar(255),
  ads_id                        bigint,
  constraint pk_ad_photos primary key (id)
);

create table ads (
  id                            bigint auto_increment not null,
  description                   varchar(255),
  price                         double not null,
  district                      varchar(255),
  thana                         varchar(255),
  phone                         varchar(255),
  chassis_number                varchar(255),
  users_nid                     varchar(255),
  constraint pk_ads primary key (id)
);

create table cycles (
  chassis_number                varchar(255) not null,
  brand                         varchar(255),
  model                         varchar(255) not null,
  price                         double,
  date_of_buy                   date,
  for_sale                      tinyint(1) default 0 not null,
  users_nid                     varchar(255),
  constraint pk_cycles primary key (chassis_number)
);

create table users (
  nid                           varchar(255) not null,
  email                         varchar(255) not null,
  district                      varchar(255),
  thana                         varchar(255),
  password                      varchar(255),
  phone                         varchar(255),
  name                          varchar(255),
  constraint pk_users primary key (nid)
);

alter table ad_photos add constraint fk_ad_photos_ads_id foreign key (ads_id) references ads (id) on delete restrict on update restrict;
create index ix_ad_photos_ads_id on ad_photos (ads_id);

alter table ads add constraint fk_ads_users_nid foreign key (users_nid) references users (nid) on delete restrict on update restrict;
create index ix_ads_users_nid on ads (users_nid);

alter table cycles add constraint fk_cycles_users_nid foreign key (users_nid) references users (nid) on delete restrict on update restrict;
create index ix_cycles_users_nid on cycles (users_nid);


# --- !Downs

alter table ad_photos drop foreign key fk_ad_photos_ads_id;
drop index ix_ad_photos_ads_id on ad_photos;

alter table ads drop foreign key fk_ads_users_nid;
drop index ix_ads_users_nid on ads;

alter table cycles drop foreign key fk_cycles_users_nid;
drop index ix_cycles_users_nid on cycles;

drop table if exists ad_photos;

drop table if exists ads;

drop table if exists cycles;

drop table if exists users;

