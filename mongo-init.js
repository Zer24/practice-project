// Переключаемся на базу данных hotel_booking_db
db = db.getSiblingDB('hotel_booking_db');

// Создаем пользователя
const userExists = db.getUser('admin');
if (!userExists) {
    db.createUser({
      user: 'admin',
      pwd: '1234',
      roles: [
        { role: 'readWrite', db: 'hotel_booking_db' }
      ]
    });
    print('User admin created successfully');
} else {
    print('User admin already exists');
}