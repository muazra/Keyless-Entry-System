__author__ = 'Muaz'

from flask import Flask, render_template, redirect, url_for, flash
from flask.ext.bootstrap import Bootstrap
from werkzeug.utils import secure_filename
from db import MongoDB
import forms

app = Flask(__name__)
app.config['SECRET_KEY'] = 'Project KES'
bootstrap = Bootstrap(app)
mongodb = MongoDB()


@app.route('/', methods=['GET', 'POST'])
def login():
    form = forms.LoginForm()

    if form.validate_on_submit():
        if mongodb.allow_login(form.username.data, form.password.data) is True:
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

        if mongodb.username_exist(form.username.data) is True:
            flash("Username already exists. Please try again.")
            return redirect(url_for('newadmin'))

        mongodb.add_admin(form.name.data, form.username.data, form.password.data, form.deviceid.data)
        mongodb.remove_device(form.deviceid.data)
        flash("Account Creation Successful. Please login below.")
        return redirect(url_for('login'))

    return render_template('newadmin.html', form=form)


@app.route('/<username>/users', methods=['GET', 'POST'])
def adminusers(username):
    form = forms.NewUserForm()

    if form.validate_on_submit():
        filename = secure_filename(form.photo.data.filename)
        form.photo.data.save('images/' + filename)
        flash(filename + "has successfully been saved")
        return redirect(url_for('adminusers', username=username))

    return render_template('adminusers.html', form=form, username=username)


@app.route('/<username>/guests', methods=['GET', 'POST'])
def adminguests(username):
    form = forms.NewGuestForm()

    if form.validate_on_submit():
        filename = secure_filename(form.photo.data.filename)
        form.photo.data.save('images/' + filename)
        return redirect(url_for('adminguests', username=username))

    return render_template('adminguests.html', form=form, username=username)


@app.route('/<username>/settings', methods=['GET', 'POST'])
def adminsettings(username):
    return render_template('adminsettings.html', username=username)


@app.route('/<username>/home', methods=['GET', 'POST'])
def adminhome(username):
    return render_template('adminhome.html', username=username)


if __name__ == '__main__':
    app.run(debug=True)