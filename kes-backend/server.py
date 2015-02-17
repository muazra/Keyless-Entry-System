__author__ = 'Muaz'

# imports
from flask import Flask, render_template, redirect, url_for, flash, session, request, jsonify
from flask.ext.bootstrap import Bootstrap
from werkzeug.utils import secure_filename
from db import MongoDB
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
        photo_filepath = "static/" + photo_simplename
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
        photo_filepath = "static/" + photo_simplename

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

        photo_simplename = "guest" + form.name.data + "_" + secure_filename(form.photo.data.filename)
        photo_filepath = "static/" + photo_simplename

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
        photo_filepath = "static/" + photo_simplename

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
    return render_template('home.html', adminprofile=adminprofile, door_activity=door_activity)


@app.route('/users/<user>', methods=['GET', 'POST'])
def usermodel(user):
    # user in this case is user's username
    if 'admin' not in session:
        return redirect(url_for('login'))

    photoform = forms.PhotoForm()
    if photoform.validate_on_submit():
        photo_simplename = "user_" + user + "_" + secure_filename(photoform.photo.data.filename)
        photo_filepath = "static/" + photo_simplename

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
        photo_filepath = "static/" + photo_simplename

        photo_add = mongodb.add_photo("guest", guest, photo_filepath, photo_simplename)
        if photo_add is -1:
            flash("Please choose photo with unique file name")
            return redirect(url_for('guestmodel', guest=guest))

        photoform.photo.data.save(photo_filepath)
        return redirect(url_for('guestmodel', guest=guest))

    guestprofile = mongodb.guest_collection.find_one({'full_name': guest})
    photos = mongodb.photo_collection.find({'profile_type': "guest", 'profile_name': guest})

    return render_template('guest.html', photoform=photoform, guestprofile=guestprofile, guestphotos=photos)


@app.route('users/<username>/remove', methods=['GET', 'POST'])
def removeuser(username):
    if 'admin' not in session:
        return redirect(url_for('login'))
    mongodb.delete_user(username)
    mongodb.delete_all_photos("user", username, os)

    flash("User successfully deleted")
    return redirect(url_for('home'))


@app.route('guests/<guestname>/remove', methods=['GET', 'POST'])
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

@app.route('/api/<key>/admin/login', methods=['GET', 'POST'])
def adminlogin(key):
    if key is not API_KEY:
        return jsonify(result="failure", details="wrong api_key")

    username = request.form['username']
    password = request.form['password']

    access = mongodb.admin_allow_login(username, password)
    if access is False:
        return jsonify(result="failure")

    profile = mongodb.admin_collection.find_one({'username': username, 'password': password})
    photos = mongodb.photo_collection.find({'profile_type': "admin", 'profile_name': username})
    return jsonify(result="success", adminprofile=profile, adminphotos=photos)


@app.route('/api/<key>/user/login', methods=['GET', 'POST'])
def userlogin(key):
    if key is not API_KEY:
        return jsonify(result="failure", details="wrong api_key")

    username = request.form['username']
    password = request.form['password']

    access = mongodb.user_allow_login(username, password)
    if access is False:
        return jsonify(result="failure")

    profile = mongodb.user_collection.find_one({'username': username, 'password': password})
    photos = mongodb.photo_collection.find({'profile_type': "user", 'profile_name': username})
    return jsonify(result="success", userprofile=profile, userphotos=photos)


@app.route('/api/<key>/<admin>/users', methods=['GET'])
def getusers(key, admin):
    if key is not API_KEY:
        return jsonify(result="failure", details="wrong api_key")

    users_list = mongodb.user_collection.find({'admin_username': admin})
    photos_list = []
    for user in users_list:
        photos = mongodb.photo_collection.find({'profile_type': "user", 'profile_name': user.get('username')})
        for photo in photos:
            photos_list.append(photo)

    return jsonify(result="success", users=users_list, photos=photos_list)


@app.route('/api/<key>/<admin>/guests', methods=['GET'])
def getguests(key, admin):
    if key is not API_KEY:
        return jsonify(result="failure", details="wrong api_key")

    guests_list = mongodb.guest_collection.find({'admin_username': admin})
    photos_list = []
    for guest in guests_list:
        photos = mongodb.photo_collection.find({'profile_type': "guest", 'profile_name': guest.get('full_name')})
        for photo in photos:
            photos_list.append(photo)

    return jsonify(result="success", guests=guests_list, photos=photos_list)


@app.route('api/<key>/photo/add', methods=['GET', 'POST'])
def addphoto(key):
    if key is not API_KEY:
        return jsonify(result="failure", details="wrong api_key")

    profile_type = request.form['profile_type']
    profile_name = request.form['profile_name']

    photo = request.files['photo']
    photo_simplename = photo.filename
    photo_filepath = "static/" + photo_simplename

    add_photo = mongodb.add_photo(profile_type, profile_name, photo_filepath, photo_simplename)
    if add_photo is -1:
        return jsonify(result="failure - choose photo with unique file name")

    photo.save(photo_filepath)
    return jsonify(result="success")


@app.route('api/<key>/photo/remove', methods=['GET', 'POST'])
def removephoto(key):
    if key is not API_KEY:
        return jsonify(result="failure", details="wrong api_key")

    photo_simplename = request.form['photo_simplename']

    delete_photo = mongodb.delete_one_photo(photo_simplename, os)
    if delete_photo is -1:
        return jsonify(result="failure - given photo with photo_simplename does not exist")

    return jsonify(result="success")


@app.route('api/<key>/user/add', methods=['GET', 'POST'])
def adduser(key):
    if key is not API_KEY:
        return jsonify(result="failure", details="wrong api_key")

    userprofile = request.get_json(force=True)
    username = userprofile.get('username')
    password = userprofile.get('password')
    full_name = userprofile.get('full_name')
    admin_username = userprofile.get('admin_username')
    admin_name = userprofile.get('admin_name')

    user_exist = mongodb.user_exist(username)
    if user_exist is True:
        return jsonify(result="failure - user already exists")

    mongodb.add_user(admin_username, admin_name, full_name, username, password)

    user_photo = request.files['user_photo']
    photo_simplename = "user_" + username + "_" + secure_filename(user_photo)
    photo_filepath = "static/" + photo_simplename

    photo_exist = mongodb.add_photo("user", username, photo_filepath, photo_simplename)
    if photo_exist is -1:
        return jsonify(result="failure - given photo with photo_simplename already exists")

    user_photo.save(photo_filepath)
    return jsonify(result="success")


@app.route('api/<key>/guest/add', methods=['GET', 'POST'])
def addguest(key):
    if key is not API_KEY:
        return jsonify(result="failure", details="wrong api_key")

    guestprofile = request.get_json(force=True)
    full_name = guestprofile.get('full_name')
    admin_username = guestprofile.get('admin_username')
    admin_name = guestprofile.get('admin_name')

    guest_exist = mongodb.guest_exist(full_name)
    if guest_exist is True:
        return jsonify(result="failure - guest already exists")

    mongodb.add_guest(admin_username, admin_name, full_name)

    guest_photo = request.files['guest_photo']
    photo_simplename = "guest_" + full_name + "_" + secure_filename(guest_photo)
    photo_filepath = "static/" + photo_simplename

    photo_exist = mongodb.add_photo("guest", full_name, photo_filepath, photo_simplename)
    if photo_exist is -1:
        return jsonify(result="failure - given photo with photo_simplename already exists")

    guest_photo.save(photo_filepath)
    return jsonify(result="success")


@app.route('api/<key>/user/delete')
def deleteuser(key):
    if key is not API_KEY:
        return jsonify(result="failure", details="wrong api_key")

    userprofile = request.get_json(force=True)
    username = userprofile.get('username')

    mongodb.delete_user(username)
    mongodb.delete_all_photos("user", username, os)
    return jsonify(result="success")


@app.route('api/<key>/guest/delete')
def deleteguest(key):
    if key is not API_KEY:
        return jsonify(result="failure", details="wrong api_key")

    guestprofile = request.get_json(force=True)
    guestname = guestprofile.get('full_name')

    mongodb.delete_guest(guestname)
    mongodb.delete_all_photos("guest", guestname, os)
    return jsonify(result="success")


# --------------------------------------------------------------------------------------------
#                                  Arduino On-Door Unit API
# --------------------------------------------------------------------------------------------

@app.route('/api/<key>/toggledoor', methods=['GET', 'POST'])
def toggledoor(key):
    if key is not API_KEY:
        return jsonify(result="failure", details="wrong api_key")

    toggleprofile = request.get_json(force=True)

    profile_type = toggleprofile.get('profile_type')
    profile_name = toggleprofile.get('name')
    photo = request.files['toggle_photo']

    # - save photo & do facial recognition
    # - create door activity record and save

    return jsonify(result="success")


if __name__ == '__main__':
    app.run(debug=True)