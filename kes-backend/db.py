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
    photo_collection = db.photo_collection

    # Device Collection

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

    # Admin Collection

    def add_admin(self, full_name, username, password, deviceid):
        username = {'full_name': full_name,
                    'username': username,
                    'password': password,
                    'deviceid': deviceid}
        self.admin_collection.insert(username)

    def admin_exist(self, username):
        if self.admin_collection.find_one({'username': username}) is None:
            return False
        return True

    def admin_allow_login(self, username, password):
        if self.admin_collection.find_one({'username': username, 'password': password}) is None:
            return False
        return True

    # User Collection

    def add_user(self, admin_username, admin_name, full_name, username, password):
        username = {'admin_username': admin_username,
                    'admin_name': admin_name,
                    'full_name': full_name,
                    'username': username,
                    'password': password}
        self.user_collection.insert(username)

    def user_exist(self, username):
        if self.user_collection.find_one({'username': username}) is None:
            return False
        return True

    def user_allow_login(self, username, password):
        if self.user_collection.find_one({'username': username, 'password': password}) is None:
            return False
        return True

    def delete_user(self, username):
        user = self.user_collection.find_one({'username': username})
        self.user_collection.remove(user)

    # Guest Collection

    def add_guest(self, admin_username, admin_name, full_name):
        name = {'admin_username': admin_username,
                'admin_name': admin_name,
                'full_name': full_name}
        self.guest_collection.insert(name)

    def guest_exist(self, guestname):
        if self.guest_collection.find_one({'full_name': guestname}) is None:
            return False
        return True

    def delete_guest(self, guestname):
        guest = self.user_collection.find_one({'full_name': guestname})
        self.guest_collection.remove(guest)

    # Photo Collection

    def add_photo(self, profile_type, username, photo_filepath, photo_simplename):
        photo_check = self.photo_collection.find_one({"photo_filepath": photo_filepath})
        if photo_check is not None:
            return -1

        photo = {'profile_type': profile_type,
                 'profile_name': username,
                 'photo_filepath': photo_filepath,
                 'photo_simplename': photo_simplename}
        self.photo_collection.insert(photo)

    def delete_all_photos(self, profile_type, username, os):
        photos = self.photo_collection.find({'profile_type': profile_type, 'profile_name': username})
        for photo in photos:
            os.remove(photo.get("photo_filepath"))
            self.photo_collection.remove(photo)

    def delete_one_photo(self, photo_simplename, os):
        photo = self.photo_collection.find_one({'photo_simplename': photo_simplename})
        os.remove(photo.get("photo_filepath"))
        self.photo_collection.remove(photo)

    # Door Toggling Activities

    # Details of what door "activity" is will need to be defined i.e. picture, guest/user, timestamp, etc.
    def add_door_activity(self, profile_type, username, details, granted):
        activity = {'profile_type': profile_type,
                    'username': username,
                    'details': details,
                    'granted': granted}

        self.door_collection.insert(activity)

    def __init__(self):
        return