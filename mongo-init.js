// Переключаемся на базу данных hotel_booking_db
db = db.getSiblingDB('hotel_booking_db');

// Создаем пользователя
db.createUser({
  user: 'hotel_admin',
  pwd: 'hotel_password_123',
  roles: [
    { role: 'readWrite', db: 'hotel_booking_db' }
  ]
});
print("User hotel_admin created successfully!");