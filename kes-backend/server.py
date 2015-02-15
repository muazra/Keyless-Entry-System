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

        filename = secure_filename(form.photo.data.filename)
        form.photo.data.save('static/' + filename)

        mongodb.add_admin(form.name.data, form.username.data, form.password.data, form.deviceid.data, filename)
        mongodb.claim_device(form.deviceid.data, form.username.data)
        flash("Account Creation Successful. Please login below.")
        session['admin'] = form.username.data
        return redirect(url_for('login'))

    return render_template('createadminaccount.html', form=form)


@app.route('/users', methods=['GET', 'POST'])
def users():
    if 'admin' not in session:
        return redirect(url_for('login'))

    form = forms.NewUserForm()
    users_list = mongodb.user_collection.find({'parent_username': session['admin']})

    if form.validate_on_submit():
        if mongodb.user_exist(form.username.data) is True:
            flash("Username already exists. Please try again.")
            return redirect(url_for('users'))

        filename = secure_filename(form.photo.data.filename)
        form.photo.data.save('static/' + filename)

        parent_name = mongodb.admin_collection.find_one({'username': session['admin']}).get("name")
        userlink = "/users/" + form.username.data

        mongodb.add_user(session['admin'], parent_name, form.name.data, form.username.data, form.password.data,
                         filename, userlink)
        flash("User successfully added")
        return redirect(url_for('users'))

    return render_template('userspanel.html', form=form, username=session['admin'], users=users_list)


@app.route('/guests', methods=['GET', 'POST'])
def guests():
    if 'admin' not in session:
        return redirect(url_for('login'))

    form = forms.NewGuestForm()
    guests_list = mongodb.guest_collection.find({'parent_username': session['admin']})

    if form.validate_on_submit():
        if mongodb.guest_exist(form.name.data) is True:
            flash("Guest already exists. Please try again.")
            return redirect(url_for('guests'))

        filename = secure_filename(form.photo.data.filename)
        form.photo.data.save('static/' + filename)

        parent_name = mongodb.admin_collection.find_one({'username': session['admin']}).get("name")
        guestlink = "/guests/" + form.name.data

        mongodb.add_guest(session['admin'], parent_name, form.name.data, filename, guestlink)
        flash("Guest successfully added")
        return redirect(url_for('guests'))

    return render_template('guestspanel.html', form=form, username=session['admin'], guests=guests_list)


@app.route('/settings', methods=['GET', 'POST'])
def settings():
    if 'admin' not in session:
        return redirect(url_for('login'))

    photoform = forms.PhotoForm()
    if photoform.validate_on_submit():
        original_filename = mongodb.admin_collection.find_one({'username': session['admin']}).get("photo")
        os.remove("static/" + original_filename)

        filename = secure_filename(photoform.photo.data.filename)
        photoform.photo.data.save('static/' + filename)
        mongodb.admin_collection.update({'username': session['admin']}, {'$set': {'photo': filename}})
        return redirect(url_for('settings'))

    adminprofile = mongodb.admin_collection.find_one({'username': session['admin']})
    adminphoto = adminprofile.get("photo")
    return render_template('settings.html', photoform=photoform, username=session['admin'],
                           adminprofile=adminprofile, adminphoto=adminphoto)


@app.route('/home', methods=['GET', 'POST'])
def home():
    adminname = mongodb.admin_collection.find_one({'username': session['admin']}).get('name')
    door_activity = mongodb.door_collection.find({'admin': session['admin']})
    return render_template('home.html', adminname=adminname, door_activity=door_activity)


@app.route('/users/<user>', methods=['GET', 'POST'])
def usermodel(user):
    if 'admin' not in session:
        return redirect(url_for('login'))

    photoform = forms.PhotoForm()
    if photoform.validate_on_submit():
        original_filename = mongodb.user_collection.find_one({'username': user}).get("photo")
        os.remove("static/" + original_filename)

        filename = secure_filename(photoform.photo.data.filename)
        photoform.photo.data.save('static/' + filename)
        mongodb.user_collection.update({'username': user}, {'$set': {'photo': filename}})
        return redirect(url_for('users'))

    userprofile = mongodb.user_collection.find_one({'username': user})
    userphoto = userprofile.get('photo')
    return render_template('user.html', photoform=photoform, username=user, userprofile=userprofile,
                           userphoto=userphoto)


@app.route('/guests/<guest>', methods=['GET', 'POST'])
def guestmodel(guest):
    if 'admin' not in session:
        return redirect(url_for('login'))

    photoform = forms.PhotoForm()
    if photoform.validate_on_submit():
        original_filename = mongodb.guest_collection.find_one({'name': guest}).get("photo")
        os.remove("static/" + original_filename)

        filename = secure_filename(photoform.photo.data.filename)
        photoform.photo.data.save('static/' + filename)
        mongodb.guest_collection.update({'name': guest}, {'$set': {'photo': filename}})
        return redirect(url_for('guests'))

    guestprofile = mongodb.guest_collection.find_one({'name': guest})
    guestphoto = guestprofile.get('photo')
    return render_template('guest.html', photoform=photoform, guestname=guest, guestprofile=guestprofile,
                           guestphoto=guestphoto)


@app.route('/users/<username>/remove', methods=['GET', 'POST'])
def removeuser(username):
    if 'admin' not in session:
        return redirect(url_for('login'))
    mongodb.delete_user(username, os)
    flash("Guest successfully added")
    return redirect(url_for('home'))


@app.route('/guests/<guestname>/remove', methods=['GET', 'POST'])
def removeguest(guestname):
    if 'admin' not in session:
        return redirect(url_for('login'))
    mongodb.delete_guest(guestname, os)
    flash("Guest successfully deleted")
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