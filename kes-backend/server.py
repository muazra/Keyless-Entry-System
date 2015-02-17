__author__ = 'Muaz'

from flask import Flask, render_template, redirect, url_for, flash, session
from flask.ext.bootstrap import Bootstrap
from werkzeug.utils import secure_filename
from db import MongoDB
from bson import json_util
import forms
import os

app = Flask(__name__)
app.config['SECRET_KEY'] = 'Project KES'
bootstrap = Bootstrap(app)
mongodb = MongoDB()

# Web App

@app.route('/', methods=['GET', 'POST'])
def login():
    if 'admin' in session:
        return redirect(url_for('home'))

    form = forms.LoginForm()
    if form.validate_on_submit():
        if mongodb.admin_allow_login(form.username.data, form.password.data) is True:
            flash("Login successful. Welcome!")
            session['admin'] = form.username.data
            return redirect(url_for('home'))
        else:
            flash("Wrong username/password")
            return redirect(url_for('login'))
    return render_template('login.html', form=form)


@app.route('/logout', methods=['GET', 'POST'])
def logout():
    if 'admin' in session:
        session.pop('admin', None)

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

        photo_simplename = secure_filename(form.photo.data.filename)
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

        photo_simplename = secure_filename(form.photo.data.filename)
        photo_filepath = "static/" + photo_simplename

        photo_add = mongodb.add_photo("user", form.username.data, photo_filepath, photo_simplename)
        if photo_add is -1:
            flash("Please choose photo with unique file name")
            return redirect(url_for('users'))

        form.photo.data.save(photo_filepath)

        admin_name = mongodb.admin_collection.find_one({'username': session['admin']}).get("full_name")
        userlink = form.username.data

        mongodb.add_user(session['admin'], admin_name, form.name.data, form.username.data,
                         form.password.data, userlink)
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

        photo_simplename = secure_filename(form.photo.data.filename)
        photo_filepath = "static/" + photo_simplename

        photo_add = mongodb.add_photo("guest", form.name.data, photo_filepath, photo_simplename)
        if photo_add is -1:
            flash("Please choose photo with unique file name")
            return redirect(url_for('users'))

        form.photo.data.save(photo_filepath)

        admin_name = mongodb.admin_collection.find_one({'username': session['admin']}).get("full_name")
        guestlink = form.name.data

        mongodb.add_guest(session['admin'], admin_name, form.name.data, guestlink)
        flash("Guest successfully added")
        return redirect(url_for('guests'))

    return render_template('guestspanel.html', form=form, guests=guests_list)


@app.route('/settings', methods=['GET', 'POST'])
def settings():
    if 'admin' not in session:
        return redirect(url_for('login'))

    photoform = forms.PhotoForm()
    if photoform.validate_on_submit():
        photo_simplename = secure_filename(photoform.photo.data.filename)
        photo_filepath = "static/" + photo_simplename

        photo_add = mongodb.add_photo("admin", session['admin'], photo_filepath, photo_simplename)
        if photo_add is -1:
            flash("Please choose photo with unique file name")
            return redirect(url_for('settings'))

        photoform.photo.data.save(photo_filepath)
        return redirect(url_for('settings'))

    adminprofile = mongodb.admin_collection.find_one({'username': session['admin']})
    photos = mongodb.photo_collection.find({'profile_type': "admin", 'profile_name': session['admin']})

    return render_template('settings.html', photoform=photoform, adminprofile=adminprofile,
                           adminphotos=photos)


@app.route('/home', methods=['GET', 'POST'])
def home():
    adminname = mongodb.admin_collection.find_one({'username': session['admin']}).get('name')
    door_activity = mongodb.door_collection.find({'admin': session['admin']})
    return render_template('home.html', adminname=adminname, door_activity=door_activity)


@app.route('/users/<user>', methods=['GET', 'POST'])
def usermodel(user):
    # user in this case is user's username
    if 'admin' not in session:
        return redirect(url_for('login'))

    photoform = forms.PhotoForm()
    if photoform.validate_on_submit():
        photo_simplename = secure_filename(photoform.photo.data.filename)
        photo_filepath = "static/" + photo_simplename

        photo_add = mongodb.add_photo("user", user, photo_filepath, photo_simplename)
        if photo_add is -1:
            flash("Please choose photo with unique file name")
            return redirect(url_for('usermodel', user=user))

        photoform.photo.data.save(photo_filepath)
        return redirect(url_for('usermodel', user=user))

    userprofile = mongodb.user_collection.find_one({'username': user})
    photos = mongodb.photo_collection.find({'profile_type': "user", 'profile_name': user})

    return render_template('user.html', photoform=photoform, username=user, userprofile=userprofile,
                           userphotos=photos)


@app.route('/guests/<guest>', methods=['GET', 'POST'])
def guestmodel(guest):
    # guest in this case is guest's name
    if 'admin' not in session:
        return redirect(url_for('login'))

    photoform = forms.PhotoForm()
    if photoform.validate_on_submit():
        photo_simplename = secure_filename(photoform.photo.data.filename)
        photo_filepath = "static/" + photo_simplename

        photo_add = mongodb.add_photo("guest", guest, photo_filepath, photo_simplename)
        if photo_add is -1:
            flash("Please choose photo with unique file name")
            return redirect(url_for('guestmodel', guest=guest))

        photoform.photo.data.save(photo_filepath)
        return redirect(url_for('guestmodel', guest=guest))

    guestprofile = mongodb.guest_collection.find_one({'full_name': guest})
    photos = mongodb.photo_collection.find({'profile_type': "guest", 'profile_name': guest})

    return render_template('guest.html', photoform=photoform, guestname=guest, guestprofile=guestprofile,
                           guestphotos=photos)


@app.route('/<username>/remove', methods=['GET', 'POST'])
def removeuser(username):
    if 'admin' not in session:
        return redirect(url_for('login'))
    mongodb.delete_user(username)
    mongodb.delete_all_photos("user", username, os)
    flash("User successfully deleted")
    return redirect(url_for('home'))


@app.route('/<guestname>/remove', methods=['GET', 'POST'])
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

# API

@app.route('/api/admins', methods=['GET'])
def getadmins():
    admins = mongodb.admin_collection.find({})
    return json_util.dumps(admins)

@app.route('/api/users', methods=['GET'])
def getusers():
    users_list = mongodb.user_collection.find({})
    return json_util.dumps(users_list)

@app.route('/api/guests', methods=['GET'])
def getguests():
    guests_list = mongodb.guest_collection.find({})
    return json_util.dumps(guests_list)

@app.route('/toggle/<encodedstring>', methods=['GET', 'POST'])
def toggledoor(encodedstring):
    fh = open("static/toggledoorimage.jpg", "wb")

    fixed = encodedstring.replace("-", "+")
    fixed = fixed.replace("_", "/")

    fh.write(fixed.decode('base64'))
    fh.close()

    return "success"

if __name__ == '__main__':
    app.run(debug=True)