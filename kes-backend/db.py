from pymongo import MongoClient


class MongoDB:
    client = MongoClient('localhost', 27017)
    db = client.db

    device_collection = db.device_collection
    device1 = {'device_id': "Device1ID"}
    device2 = {'device_id': "Device2ID"}
    device_collection.insert(device1)
    device_collection.insert(device2)

    user_collection = db.user_collection

    def device_exist(self, device_id):
        if self.device_collection.find_one({'device_id': device_id}) is None:
            return False
        return True

    def remove_device(self, device_id):
        self.device_collection.remove({'device_id': device_id})

    def add_user(self, name, username, password, deviceid):
        username = {'name': name,
                    'username': username,
                    'password': password,
                    'deviceid': deviceid}
        self.user_collection.insert(username)

    def username_exist(self, username):
        if self.user_collection.find_one({'username': username}) is None:
            return False
        else:
            return True

    def allow_login(self, username, password):
        if self.user_collection.find_one({'username': username, 'password': password}) is None:
            return False
        else:
            return True

    def __init__(self):
        return