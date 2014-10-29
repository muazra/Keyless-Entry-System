__author__ = 'Muaz'

from flask import Flask, render_template, redirect, url_for, flash, json
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


@app.route('/', methods=['GET', 'POST'])
def login():
    form = forms.LoginForm()

    if form.validate_on_submit():
        if mongodb.admin_allow_login(form.username.data, form.password.data) is True:
            flash("Login successful. Welcome!")
            return redirect(url_for('adminhome', username=form.username.data))
        else:
            flash("Wrong username/password")
            return redirect(url_for('login'))
    return render_template('login.html', form=form)


@app.route('/newadmin', methods=['GET', 'POST'])
def newadmin():
    form = forms.NewAdminForm()

    if form.validate_on_submit():
        if mongodb.device_exist(form.deviceid.data) is False:
            flash("Device ID Not Found!")
            return redirect(url_for('newadmin'))

        if mongodb.admin_exist(form.username.data) is True:
            flash("Username already exists. Please try again.")
            return redirect(url_for('newadmin'))

        filename = secure_filename(form.photo.data.filename)
        form.photo.data.save('static/' + filename)

        mongodb.add_admin(form.name.data, form.username.data, form.password.data, form.deviceid.data, filename)
        mongodb.remove_device(form.deviceid.data)
        flash("Account Creation Successful. Please login below.")
        return redirect(url_for('login'))

    return render_template('newadmin.html', form=form)


@app.route('/<username>/users', methods=['GET', 'POST'])
def adminusers(username):
    form = forms.NewUserForm()
    users = mongodb.user_collection.find({'parent_username': username})

    if form.validate_on_submit():
        if mongodb.user_exist(form.username.data) is True:
            flash("Username already exists. Please try again.")
            return redirect(url_for('adminusers', username=username))

        filename = secure_filename(form.photo.data.filename)
        form.photo.data.save('static/' + filename)

        parent_name = mongodb.admin_collection.find_one({'username': username}).get("name")
        userlink = "/" + username + "/users" + "/" + form.username.data

        mongodb.add_user(username, parent_name, form.name.data, form.username.data, form.password.data, filename, userlink)
        flash("User has successfully been added")
        return redirect(url_for('adminusers', username=username))

    return render_template('adminusers.html', form=form, username=username, users=users)


@app.route('/<username>/guests', methods=['GET', 'POST'])
def adminguests(username):
    form = forms.NewGuestForm()
    guests = mongodb.guest_collection.find({'parent_username': username})

    if form.validate_on_submit():
        if mongodb.guest_exist(form.name.data) is True:
            flash("Guest already exists. Please try again.")
            return redirect(url_for('adminguests', username=username))

        filename = secure_filename(form.photo.data.filename)
        form.photo.data.save('static/' + filename)

        parent_name = mongodb.admin_collection.find_one({'username': username}).get("name")
        guestlink = "/" + username + "/guests" + "/" + form.name.data

        mongodb.add_guest(username, parent_name, form.name.data, filename, guestlink)
        flash("Guest has successfully been added")
        return redirect(url_for('adminguests', username=username))

    return render_template('adminguests.html', form=form, username=username, guests=guests)


@app.route('/<username>/settings', methods=['GET', 'POST'])
def adminsettings(username):
    photoform = forms.PhotoForm()
    if photoform.validate_on_submit():
        original_filename = mongodb.admin_collection.find_one({'username': username}).get("photo")
        os.remove("static/" + original_filename)

        filename = secure_filename(photoform.photo.data.filename)
        photoform.photo.data.save('static/' + filename)
        mongodb.admin_collection.update({'username': username}, {'$set': {'photo': filename}})
        return redirect(url_for('adminsettings', username=username))

    adminprofile = mongodb.admin_collection.find_one({'username': username})
    adminphoto = adminprofile.get("photo")
    return render_template('adminsettings.html', photoform=photoform, username=username, adminprofile=adminprofile,
                           adminphoto=adminphoto)

@app.route('/<username>/home', methods=['GET', 'POST'])
def adminhome(username):
    adminname = mongodb.admin_collection.find_one({'username': username}).get('name')
    return render_template('adminhome.html', username=username, adminname=adminname)

@app.route('/<username>/users/<user>', methods=['GET', 'POST'])
def usermodel(username, user):
    photoform = forms.PhotoForm()
    if photoform.validate_on_submit():
        original_filename = mongodb.user_collection.find_one({'username': user}).get("photo")
        os.remove("static/" + original_filename)

        filename = secure_filename(photoform.photo.data.filename)
        photoform.photo.data.save('static/' + filename)
        mongodb.user_collection.update({'username': user}, {'$set': {'photo': filename}})
        return redirect(url_for('usermodel', username=username, user=user))

    userprofile = mongodb.user_collection.find_one({'username': user})
    userphoto = userprofile.get('photo')
    return render_template('user.html', photoform=photoform, username=username, userprofile=userprofile,
                           userphoto=userphoto)

@app.route('/<username>/guests/<guest>', methods=['GET', 'POST'])
def guestmodel(username, guest):
    photoform = forms.PhotoForm()
    if photoform.validate_on_submit():
        original_filename = mongodb.guest_collection.find_one({'name': guest}).get("photo")
        os.remove("static/" + original_filename)

        filename = secure_filename(photoform.photo.data.filename)
        photoform.photo.data.save('static/' + filename)
        mongodb.guest_collection.update({'name': guest}, {'$set': {'photo': filename}})
        return redirect(url_for('guestmodel', username=username, guest=guest))

    guestprofile = mongodb.guest_collection.find_one({'name': guest})
    guestphoto = guestprofile.get('photo')
    return render_template('guest.html', photoform=photoform, username=username, guestprofile=guestprofile,
                           guestphoto=guestphoto)

@app.route('/api/admins')
def getadmins():
    admins = mongodb.admin_collection.find({})
    return json_util.dumps(admins)

@app.route('/api/users')
def getusers():
    users = mongodb.user_collection.find({})
    return json_util.dumps(users)

@app.route('/api/guests')
def getguests():
    guests = mongodb.guest_collection.find({})
    return json_util.dumps(guests)

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