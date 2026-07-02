import bcrypt
salt = bcrypt.gensalt(rounds=10)
hashed = bcrypt.hashpw(b"123456", salt)
print(hashed.decode('utf-8').replace('$2b$', '$2a$'))
