__author__ = 'Muaz'

try:
    import simplejson as json
except ImportError:
    try:
        import json
    except ImportError:
        raise ImportError
import datetime
from bson.objectid import ObjectId
from werkzeug import Response
from db import MongoDB

PROJECT_DIRECTORY = "/Users/Muaz/Documents/Github_Projects/Project-KES/kes-backend/static/training/"

mongodb = MongoDB()


class MongoJsonEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, (datetime.datetime, datetime.date)):
            return obj.isoformat()
        elif isinstance(obj, ObjectId):
            return unicode(obj)
        return json.JSONEncoder.default(self, obj)


def jsonify(*args, **kwargs):
    """ jsonify with support for MongoDB ObjectId
    """
    return Response(json.dumps(dict(*args, **kwargs), cls=MongoJsonEncoder), mimetype='application/json')


def create_csv():
    photos = mongodb.photo_collection.find()
    f = open('csv.txt', 'w')

    label = -1
    profile_folder = ""

    photos_dict = {}

    for photo in photos:
        profile_type = photo.get("profile_type")
        photo_name = photo.get("photo_simplename")
        profile_name = photo.get("profile_name")

        if photos_dict.get(profile_name) is not None:
            current_label = photos_dict.get(profile_name)
        else:
            label += 1
            current_label = label

        if profile_type == "admin":
            profile_folder = "admin_photos/"
        elif profile_type == "user":
            profile_folder = "user_photos/"
        elif profile_type == "guest":
            profile_folder = "guest_photos/"

        f.write(PROJECT_DIRECTORY + profile_folder + photo_name + ";" + str(current_label) + "\n")
        photos_dict[profile_name] = current_label

    f.close()
    return ""