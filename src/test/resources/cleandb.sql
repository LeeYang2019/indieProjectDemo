delete from user;
Alter table user AUTO_INCREMENT = 0;
INSERT INTO user VALUES (1, 'leeyang2019', 'greenbayPackers'),(2, 'johnBones2019', 'greenMantaRays') ;
delete from yugioh_card;
Alter table yugioh_card AUTO_INCREMENT = 0;
INSERT INTO `yugioh_card` VALUES (1,1,'Dark Magician','Monster'),(2,1,'Dark Magician Girl','Monster');
delete from yugioh_card_history;
Alter table yugioh_card_history AUTO_INCREMENT = 0;
INSERT INTO `yugioh_card_history` VALUES (1,1,now(),15.00);