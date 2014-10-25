from pymongo import MongoClient


class MongoDB:
    client = MongoClient('localhost', 27017)
    db = client.db

    device_collection = db.device_collection
    devices = {'device_id': 'abc123'}
    device_collection.insert(devices)

    user_collection = db.user_collection

    def device_exist(self, device_id):
        if self.device_collection.find_one({'device_id': device_id}) is None:
            return False
        return True

    def add_user(self, name, username, password, deviceid):
        username = {'name': name,
                    'username': username,
                    'password': password,
                    'deviceid': deviceid}
        self.user_collection.insert(username)

    def user_exist(self, username):
        if self.user_collection.find_one({'username': username}) is None:
            return False
        return True

    def __init__(self):
        return