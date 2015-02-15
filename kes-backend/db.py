from pymongo import MongoClient


class MongoDB:
    client = MongoClient('localhost', 27017)
    db = client.db

    device_collection = db.device_collection
    device1 = {'device_id': "Device1ID",
               'available': "true",
               'admin': "n/a",
               'status': "locked",
               'battery': "100%"}
    device_collection.insert(device1)

    device2 = {'device_id': "Device2ID",
               'available': "true",
               'admin': "n/a",
               'status': "locked",
               'battery': "100%"}
    device_collection.insert(device2)

    admin_collection = db.admin_collection
    user_collection = db.user_collection
    guest_collection = db.guest_collection
    door_collection = db.door_collection

    def device_available(self, device_id):
        if self.device_collection.find_one({'device_id': device_id, 'available': "true"}) is None:
            return False
        return True

    def claim_device(self, device_id, admin):
        self.device_collection.update({'device_id': device_id}, {'$set': {'available': "false", 'admin': admin}})

    def update_device_status(self, device_id, status):
        self.device_collection.update({'device_id': device_id}, {'$set': {'status': status}})

    def update_device_battery(self, device_id, battery):
        self.device_collection.update({'device_id': device_id}, {'$set': {'battery': battery + "%"}})

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
        return True

    def admin_allow_login(self, username, password):
        if self.admin_collection.find_one({'username': username, 'password': password}) is None:
            return False
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
        return True

    def delete_user(self, username, os):
        user = self.user_collection.find_one({'username': username})
        os.remove("static/" + user.get("photo"))
        self.user_collection.remove(user)

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
        return True

    def delete_guest(self, guestname, os):
        guest = self.user_collection.find_one({'name': guestname})
        os.remove("static/" + guestname.get("photo"))
        self.guest_collection.remove(guest)

    # Details of what door "activity" is will need to be defined i.e. picture, guest/user, timestamp, etc.
    def add_door_activity(self, admin, activity):
        activity = {'admin': admin,
                    'details': activity}
        self.door_collection.insert(activity)

    def __init__(self):
        return