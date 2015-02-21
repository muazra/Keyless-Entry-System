__author__ = 'Muaz'

# imports
from flask import Flask, render_template, redirect, url_for, flash, session, request
from flask.ext.bootstrap import Bootstrap
from werkzeug.utils import secure_filename
from db import MongoDB
from tools import jsonify
import forms
import os

# configurations
API_KEY = "KES_MJN"

# launch
app = Flask(__name__)
app.config['SECRET_KEY'] = 'Project KES'
bootstrap = Bootstrap(app)
mongodb = MongoDB()


# --------------------------------------------------------------------------------------------
#                                           Web App
# --------------------------------------------------------------------------------------------

@app.route('/', methods=['GET', 'POST'])
def login():
    if 'admin' in session:
        return redirect(url_for('home'))

    form = forms.LoginForm()
    if form.validate_on_submit():
        if mongodb.admin_allow_login(form.username.data, form.password.data) is True:
            flash("Login successful. Welcome!")
            session['admin'] = form.username.data
            session['admin_name'] = mongodb.admin_collection.find_one({'username': form.username.data}).get("full_name")
            return redirect(url_for('home'))
        else:
            flash("Wrong username/password")
            return redirect(url_for('login'))
    return render_template('login.html', form=form)


@app.route('/logout', methods=['GET', 'POST'])
def logout():
    if 'admin' in session:
        session.pop('admin', None)
        session.pop('admin_name', None)

    return redirect(url_for('login'))


@app.route('/newadmin', methods=['GET', 'POST'])
def newadmin():
    if 'admin' in session:
        return redirect(url_for('home'))

    form = forms.NewAdminForm()
    if form.validate_on_submit():
        if mongodb.device_available(form.deviceid.data) is False:
            flash("Device ID Not Found!")
            return redirect(url_for('newadmin'))

        if mongodb.admin_exist(form.username.data) is True:
            flash("Username already exists. Please try again.")
            return redirect(url_for('newadmin'))

        photo_simplename = "admin_" + form.username.data + "_" + secure_filename(form.photo.data.filename)
        photo_filepath = "static/" + "admin_photos/" + photo_simplename
        form.photo.data.save(photo_filepath)

        mongodb.add_admin(form.name.data, form.username.data, form.password.data, form.deviceid.data)
        photo_add = mongodb.add_photo("admin", form.username.data, photo_filepath, photo_simplename)

        if photo_add is -1:
            flash("Please choose photo with unique file name")
            return redirect(url_for('newadmin'))

        mongodb.claim_device(form.deviceid.data, form.username.data)
        flash("Account Creation Successful. Welcome.")
        session['admin'] = form.username.data
        session['admin_name'] = form.name.data
        return redirect(url_for('login'))

    return render_template('createadminaccount.html', form=form)


@app.route('/users', methods=['GET', 'POST'])
def users():
    if 'admin' not in session:
        return redirect(url_for('login'))

    form = forms.NewUserForm()
    users_list = mongodb.user_collection.find({'admin_username': session['admin']})

    if form.validate_on_submit():
        if mongodb.user_exist(form.username.data) is True:
            flash("Username already exists. Please try again.")
            return redirect(url_for('users'))

        photo_simplename = "user_" + form.username.data + "_" + secure_filename(form.photo.data.filename)
        photo_filepath = "static/" + "user_photos/" + photo_simplename

        photo_add = mongodb.add_photo("user", form.username.data, photo_filepath, photo_simplename)
        if photo_add is -1:
            flash("Please choose photo with unique file name")
            return redirect(url_for('users'))

        form.photo.data.save(photo_filepath)
        mongodb.add_user(session['admin'], session['admin_name'], form.name.data,
                         form.username.data, form.password.data)

        flash("User successfully added")
        return redirect(url_for('users'))

    return render_template('userspanel.html', form=form, users=users_list)


@app.route('/guests', methods=['GET', 'POST'])
def guests():
    if 'admin' not in session:
        return redirect(url_for('login'))

    form = forms.NewGuestForm()
    guests_list = mongodb.guest_collection.find({'admin_username': session['admin']})

    if form.validate_on_submit():
        if mongodb.guest_exist(form.name.data) is True:
            flash("Guest already exists. Please try again.")
            return redirect(url_for('guests'))

        photo_simplename = "guest_" + form.name.data + "_" + secure_filename(form.photo.data.filename)
        photo_filepath = "static/" + "guest_photos/" + photo_simplename

        photo_add = mongodb.add_photo("guest", form.name.data, photo_filepath, photo_simplename)
        if photo_add is -1:
            flash("Please choose photo with unique file name")
            return redirect(url_for('users'))

        form.photo.data.save(photo_filepath)
        mongodb.add_guest(session['admin'], session['admin_name'], form.name.data)

        flash("Guest successfully added")
        return redirect(url_for('guests'))

    return render_template('guestspanel.html', form=form, guests=guests_list)


@app.route('/settings', methods=['GET', 'POST'])
def settings():
    if 'admin' not in session:
        return redirect(url_for('login'))

    photoform = forms.PhotoForm()
    if photoform.validate_on_submit():
        photo_simplename = "admin_" + session['admin'] + "_" + secure_filename(photoform.photo.data.filename)
        photo_filepath = "static/" + "admin_photos/" + photo_simplename

        photo_add = mongodb.add_photo("admin", session['admin'], photo_filepath, photo_simplename)
        if photo_add is -1:
            flash("Please choose photo with unique file name")
            return redirect(url_for('settings'))

        photoform.photo.data.save(photo_filepath)
        return redirect(url_for('settings'))

    adminprofile = mongodb.admin_collection.find_one({'username': session['admin']})
    photos = mongodb.photo_collection.find({'profile_type': "admin", 'profile_name': session['admin']})

    return render_template('settings.html', photoform=photoform, adminprofile=adminprofile, adminphotos=photos)


@app.route('/home', methods=['GET', 'POST'])
def home():
    adminprofile = mongodb.admin_collection.find_one({'username': session['admin']})
    door_activity = mongodb.door_collection.find({'admin': session['admin']})
    device = mongodb.device_collection.find_one({'admin': session['admin']})
    return render_template('home.html', adminprofile=adminprofile, door_activity=door_activity, device=device)


@app.route('/users/<user>', methods=['GET', 'POST'])
def usermodel(user):
    # user in this case is user's username
    if 'admin' not in session:
        return redirect(url_for('login'))

    photoform = forms.PhotoForm()
    if photoform.validate_on_submit():
        photo_simplename = "user_" + user + "_" + secure_filename(photoform.photo.data.filename)
        photo_filepath = "static/" + "user_photos/" + photo_simplename

        photo_add = mongodb.add_photo("user", user, photo_filepath, photo_simplename)
        if photo_add is -1:
            flash("Please choose photo with unique file name")
            return redirect(url_for('usermodel', user=user))

        photoform.photo.data.save(photo_filepath)
        return redirect(url_for('usermodel', user=user))

    userprofile = mongodb.user_collection.find_one({'username': user})
    photos = mongodb.photo_collection.find({'profile_type': "user", 'profile_name': user})

    return render_template('user.html', photoform=photoform, userprofile=userprofile, userphotos=photos)


@app.route('/guests/<guest>', methods=['GET', 'POST'])
def guestmodel(guest):
    # guest in this case is guest's name
    if 'admin' not in session:
        return redirect(url_for('login'))

    photoform = forms.PhotoForm()
    if photoform.validate_on_submit():
        photo_simplename = "guest_" + guest + "_" + secure_filename(photoform.photo.data.filename)
        photo_filepath = "static/" + "guest_photos/" + photo_simplename

        photo_add = mongodb.add_photo("guest", guest, photo_filepath, photo_simplename)
        if photo_add is -1:
            flash("Please choose photo with unique file name")
            return redirect(url_for('guestmodel', guest=guest))

        photoform.photo.data.save(photo_filepath)
        return redirect(url_for('guestmodel', guest=guest))

    guestprofile = mongodb.guest_collection.find_one({'full_name': guest})
    photos = mongodb.photo_collection.find({'profile_type': "guest", 'profile_name': guest})

    return render_template('guest.html', photoform=photoform, guestprofile=guestprofile, guestphotos=photos)


@app.route('/users/<username>/remove', methods=['GET', 'POST'])
def removeuser(username):
    if 'admin' not in session:
        return redirect(url_for('login'))
    mongodb.delete_user(username)
    mongodb.delete_all_photos("user", username, os)

    flash("User successfully deleted")
    return redirect(url_for('home'))


@app.route('/guests/<guestname>/remove', methods=['GET', 'POST'])
def removeguest(guestname):
    if 'admin' not in session:
        return redirect(url_for('login'))
    mongodb.delete_guest(guestname)
    mongodb.delete_all_photos("guest", guestname, os)

    flash("Guest successfully deleted")
    return redirect(url_for('home'))


@app.route('/removephoto/<photosimplename>', methods=['GET', 'POST'])
def removephoto(photosimplename):
    if 'admin' not in session:
        return redirect(url_for('login'))
    if photosimplename is not None:
        mongodb.delete_one_photo(photosimplename, os)
        flash("Image deleted")
        return redirect(url_for('home'))

# --------------------------------------------------------------------------------------------
#                                  Mobile Application API
# --------------------------------------------------------------------------------------------


def api_log_welcome():
    return jsonify(result="Welcome to the official API of Project KES")


def api_log_wrong_key():
    return jsonify(result="failure", details="wrong api_key")


def api_log_wrong_credentials():
    return jsonify(result="failure", details="login credentials not accurate")


@app.route('/api', methods=['GET'])
def api_home():
    return api_log_welcome()


@app.route('/api/<key>/newadmin', methods=['GET', 'POST'])
def api_newadmin(key):
    if key != API_KEY:
        return api_log_wrong_key()

    username = request.form['username']
    password = request.form['password']
    full_name = request.form['full_name']
    device_id = request.form['device_id']
    admin_photo = request.files['admin_photo']

    if mongodb.device_available(device_id) is False:
        return jsonify(result="failure", details="given device not available")

    if mongodb.admin_exist(username) is True:
        return jsonify(result="failure", details="given username not available")

    photo_simplename = "admin_" + username + "_" + secure_filename(admin_photo.filename)
    photo_filepath = "static/" + "admin_photos/" + photo_simplename

    mongodb.add_photo("admin", username, photo_filepath, photo_simplename)
    mongodb.add_admin(full_name, username, password, device_id)
    mongodb.claim_device(device_id, username)
    admin_photo.save(photo_filepath)

    return jsonify(result="success")


@app.route('/api/<key>/admin/login', methods=['GET', 'POST'])
def api_adminlogin(key):
    if key != API_KEY:
        return api_log_wrong_key()

    username = request.form['username']
    password = request.form['password']

    access = mongodb.admin_allow_login(username, password)
    if access is False:
        return api_log_wrong_credentials()

    profile = mongodb.admin_collection.find_one({'username': username, 'password': password})
    device = mongodb.device_collection.find_one({'admin': username})

    door_activity = mongodb.door_collection.find({'admin': username})
    door_list = []
    for door in door_activity:
        door_list.append(door)

    photos = mongodb.photo_collection.find({'profile_type': "admin", 'profile_name': username})
    photos_list = []
    for photo in photos:
        photos_list.append(photo)

    return jsonify(result="success", profile=profile, device=device, photos=photos_list, door=door_list)


@app.route('/api/<key>/getadmin', methods=['GET', 'POST'])
def api_getadmin(key):
    if key != API_KEY:
        return api_log_wrong_key()

    admin = request.form['admin']

    profile = mongodb.admin_collection.find_one({'username': admin})
    photos = mongodb.photo_collection.find({'profile_type': "admin", 'profile_name': admin})
    photos_list = []
    for photo in photos:
        photos_list.append(photo)

    return jsonify(result="success", profile=profile, photos=photos_list)


@app.route('/api/<key>/getuser', methods=['GET', 'POST'])
def api_getuser(key):
    if key != API_KEY:
        return api_log_wrong_key()

    user = request.form['user']

    profile = mongodb.user_collection.find_one({'username': user})
    photos = mongodb.photo_collection.find({'profile_type': "user", 'profile_name': user})
    photos_list = []
    for photo in photos:
        photos_list.append(photo)

    return jsonify(result="success", profile=profile, photos=photos_list)


@app.route('/api/<key>/getguest', methods=['GET', 'POST'])
def api_getguest(key):
    if key != API_KEY:
        return api_log_wrong_key()

    guest = request.form['guest']

    profile = mongodb.guest_collection.find_one({'full_name': guest})
    photos = mongodb.photo_collection.find({'profile_type': "guest", 'profile_name': guest})
    photos_list = []
    for photo in photos:
        photos_list.append(photo)

    return jsonify(result="success", profile=profile, photos=photos_list)


@app.route('/api/<key>/user/login', methods=['GET', 'POST'])
def api_userlogin(key):
    if key != API_KEY:
        return api_log_wrong_key()

    username = request.form['username']
    password = request.form['password']

    access = mongodb.user_allow_login(username, password)
    if access is False:
        return api_log_wrong_credentials()

    profile = mongodb.user_collection.find_one({'username': username, 'password': password})
    photos = mongodb.photo_collection.find({'profile_type': "user", 'profile_name': username})
    photos_list = []
    for photo in photos:
        photos_list.append(photo)

    device = mongodb.device_collection.find_one({'admin': profile.get('admin_username')})
    door_activity = mongodb.door_collection.find({'admin': profile.get('admin_username')})
    door_list = []
    for door in door_activity:
        door_list.append(door)

    return jsonify(result="success", profile=profile, device=device, photos=photos_list, door=door_list)


@app.route('/api/<key>/users', methods=['GET', 'POST'])
def api_getusers(key):
    if key != API_KEY:
        return api_log_wrong_key()

    admin = request.form['admin']

    users_query = mongodb.user_collection.find({'admin_username': admin})
    users_list = []
    for user in users_query:
        users_list.append(user)

    photos_list = []
    for user in users_list:
        photos = mongodb.photo_collection.find({'profile_type': "user", 'profile_name': user.get('username')})
        for photo in photos:
            photos_list.append(photo)

    return jsonify(result="success", users=users_list, photos=photos_list)


@app.route('/api/<key>/guests', methods=['GET', 'POST'])
def api_getguests(key):
    if key != API_KEY:
        return api_log_wrong_key()

    admin = request.form['admin']

    guests_query = mongodb.guest_collection.find({'admin_username': admin})
    guests_list = []
    for guest in guests_query:
        guests_list.append(guest)

    photos_list = []
    for guest in guests_list:
        photos = mongodb.photo_collection.find({'profile_type': "guest", 'profile_name': guest.get('full_name')})
        for photo in photos:
            photos_list.append(photo)

    return jsonify(result="success", guests=guests_list, photos=photos_list)


@app.route('/api/<key>/photo/add', methods=['GET', 'POST'])
def api_addphoto(key):
    if key != API_KEY:
        return api_log_wrong_key()

    profile_type = request.form['profile_type']
    profile_name = request.form['profile_name']

    photo = request.files['photo']
    photo_simplename = profile_type + "_" + profile_name + "_" + secure_filename(photo.filename)
    photo_filepath = "static/" + profile_type + "_photos/" + photo_simplename

    add_photo = mongodb.add_photo(profile_type, profile_name, photo_filepath, photo_simplename)
    if add_photo is -1:
        return jsonify(result="failure", details="choose photo with unique file name")

    photo.save(photo_filepath)
    return jsonify(result="success", details="photo add successful")


@app.route('/api/<key>/photo/remove', methods=['GET', 'POST'])
def api_removephoto(key):
    if key != API_KEY:
        return api_log_wrong_key()

    photo_simplename = request.form['photo_simplename']

    delete_photo = mongodb.delete_one_photo(photo_simplename, os)
    if delete_photo is -1:
        return jsonify(result="failure", details="given photo with photo_simplename does not exist")

    return jsonify(result="success", details="photo delete successful")


@app.route('/api/<key>/user/add', methods=['GET', 'POST'])
def api_adduser(key):
    if key != API_KEY:
        return api_log_wrong_key()

    username = request.form['username']
    password = request.form['password']
    full_name = request.form['full_name']
    admin_username = request.form['admin_username']

    admin = mongodb.admin_collection.find_one({'username': admin_username})
    admin_name = admin.get("full_name")

    user_exist = mongodb.user_exist(username)
    if user_exist is True:
        return jsonify(result="failure", details="user already exists")

    user_photo = request.files['user_photo']
    photo_simplename = "user_" + username + "_" + secure_filename(user_photo.filename)
    photo_filepath = "static/" + "user_photos/" + photo_simplename

    mongodb.add_photo("user", username, photo_filepath, photo_simplename)
    mongodb.add_user(admin_username, admin_name, full_name, username, password)
    user_photo.save(photo_filepath)

    return jsonify(result="success")


@app.route('/api/<key>/guest/add', methods=['GET', 'POST'])
def api_addguest(key):
    if key != API_KEY:
        return api_log_wrong_key()

    full_name = request.form['full_name']
    admin_username = request.form['admin_username']

    admin = mongodb.admin_collection.find_one({'username': admin_username})
    admin_name = admin.get("full_name")

    guest_exist = mongodb.guest_exist(full_name)
    if guest_exist is True:
        return jsonify(result="failure", details="guest already exists")

    guest_photo = request.files['guest_photo']
    photo_simplename = "guest_" + full_name + "_" + secure_filename(guest_photo.filename)
    photo_filepath = "static/" + "guest_photos/" + photo_simplename

    mongodb.add_photo("guest", full_name, photo_filepath, photo_simplename)
    mongodb.add_guest(admin_username, admin_name, full_name)
    guest_photo.save(photo_filepath)

    return jsonify(result="success")


@app.route('/api/<key>/user/delete', methods=['GET', 'POST'])
def api_deleteuser(key):
    if key != API_KEY:
        return api_log_wrong_key()

    username = request.form['username']

    mongodb.delete_user(username)
    mongodb.delete_all_photos("user", username, os)
    return jsonify(result="success")


@app.route('/api/<key>/guest/delete', methods=['GET', 'POST'])
def api_deleteguest(key):
    if key != API_KEY:
        return api_log_wrong_key()

    guestname = request.form['full_name']

    mongodb.delete_guest(guestname)
    mongodb.delete_all_photos("guest", guestname, os)
    return jsonify(result="success")


@app.route('/api/<key>/dashinfo', methods=['GET', 'POST'])
def api_dashinfo(key):
    if key != API_KEY:
        return api_log_wrong_key()

    admin = request.form['admin']
    profile = mongodb.admin_collection.find_one({'username': admin})

    device = mongodb.device_collection.find_one({'admin': admin})

    door_activity = mongodb.door_collection.find({'admin': admin})
    door_list = []
    for door in door_activity:
        door_list.append(door)

    return jsonify(result="success", profile=profile, device=device, door=door_list)


# --------------------------------------------------------------------------------------------
#                                     Door Unit API
# --------------------------------------------------------------------------------------------

@app.route('/api/<key>/toggledoorstatus', methods=['GET', 'POST'])
def api_toggledoorstatus(key):
    if key != API_KEY:
        return api_log_wrong_key()

    device_id = request.form['device_id']
    status = request.form['status']

    mongodb.update_device_status(device_id, status)
    return jsonify(result="success")


@app.route('/api/<key>/updatedoorbattery', methods=['GET', 'POST'])
def api_updatedoorbattery(key):
    if key != API_KEY:
        return api_log_wrong_key()

    device_id = request.form['device_id']
    battery = request.form['battery']

    mongodb.update_device_battery(device_id, battery)
    return jsonify(result="success")


@app.route('/api/<key>/dooractivity', methods=['GET', 'POST'])
def api_dooractivity(key):
    if key != API_KEY:
        return api_log_wrong_key()

    admin = request.form['admin']

    if mongodb.admin_exist(admin) is False:
        return jsonify(result="failure", details="given admin does not exist")

    door_activities = mongodb.door_collection.find({'admin': admin})
    door_activity_list = []
    for activity in door_activities:
        door_activity_list.append(activity)

    return jsonify(result="success", door_activities=door_activity_list)


@app.route('/api/<key>/toggledoor', methods=['GET', 'POST'])
def api_toggledoor(key):
    if key != API_KEY:
        return api_log_wrong_key()

    # gather request information
    admin = request.form['admin']
    profile_type = request.form['profile_type']
    profile_name = request.form['profile_name']
    device_id = request.form['device_id']
    toggle_photo = request.files['toggle_photo']

    # save toggle photo
    photo_simplename = "toggle_" + profile_type + "_" + profile_name + "_" + secure_filename(toggle_photo.filename)
    photo_filepath = "static/" + "toggle_photos/" + photo_simplename
    toggle_photo.save(photo_filepath)

    # add toggle activity
    mongodb.add_toggle_activity(profile_type, profile_name, photo_simplename, photo_filepath)

    # do facial recognition
    access = facialrecognition()

    # delete toggle activity (depending on our system design)
    mongodb.delete_toggle_activity(photo_simplename)

    # - create door activity record and save
    mongodb.add_door_activity(admin, profile_type, profile_name, photo_simplename, access)

    # - change device status
    status = "locked"
    if access:
        status = "unlocked"
    mongodb.update_device_status(device_id, status)

    return jsonify(result="success")


def facialrecognition():
    return True

if __name__ == '__main__':
    app.run(debug=True)