from pymongo import MongoClient


class MongoDB:
    client = MongoClient('localhost', 27017)
    db = client.db

    device_collection = db.device_collection
    device1 = {'device_id': "Device1ID"}
    device2 = {'device_id': "Device2ID"}
    device_collection.insert(device1)
    device_collection.insert(device2)

    admin_collection = db.admin_collection
    user_collection = db.user_collection
    guest_collection = db.guest_collection

    def device_exist(self, device_id):
        if self.device_collection.find_one({'device_id': device_id}) is None:
            return False
        return True

    def remove_device(self, device_id):
        self.device_collection.remove({'device_id': device_id})

    def add_admin(self, name, username, password, deviceid, photo):
        username = {'name': name,
                    'username': username,
                    'password': password,
                    'deviceid': deviceid,
                    'photo': photo}
        self.admin_collection.insert(username)

    def admin_exist(self, username):
        if self.admin_collection.find_one({'username': username}) is None:
            return False
        else:
            return True

    def admin_allow_login(self, username, password):
        if self.admin_collection.find_one({'username': username, 'password': password}) is None:
            return False
        else:
            return True

    def add_user(self, parent_username, parent_name, name, username, password, photo, userlink):
        username = {'parent_username': parent_username,
                    'parent_name': parent_name,
                    'name': name,
                    'username': username,
                    'password': password,
                    'photo': photo,
                    'link': userlink}
        self.user_collection.insert(username)

    def user_exist(self, username):
        if self.user_collection.find_one({'username': username}) is None:
            return False
        else:
            return True

    def add_guest(self, parent_username, parent_name, name, photo, guestlink):
        name = {'parent_username': parent_username,
                'parent_name': parent_name,
                'name': name,
                'photo': photo,
                'link': guestlink}
        self.guest_collection.insert(name)

    def guest_exist(self, name):
        if self.guest_collection.find_one({'name': name}) is None:
            return False
        else:
            return True

    def __init__(self):
        return