__author__ = 'Muaz'

from flask import Flask, render_template, redirect, url_for, flash
from flask.ext.bootstrap import Bootstrap
from flask.ext.wtf import Form
from wtforms import StringField, SubmitField, PasswordField
from wtforms.validators import DataRequired
from db import MongoDB

app = Flask(__name__)
app.config['SECRET_KEY'] = 'Project KES'
bootstrap = Bootstrap(app)
mongodb = MongoDB()


class LoginForm(Form):
    username = StringField('Username', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    submit = SubmitField('Submit')


class NewUserForm(Form):
    name = StringField('Name', validators=[DataRequired()])
    username = StringField('Username', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    deviceid = StringField('Device ID', validators=[DataRequired()])
    submit = SubmitField('Submit')


@app.route('/', methods=['GET', 'POST'])
def login():
    form = LoginForm()

    if form.validate_on_submit():
        if mongodb.allow_login(form.username.data, form.password.data) is True:
            flash("Login successful. Welcome!")
            return redirect(url_for('userhome', username=form.username.data))
        else:
            flash("Wrong username/password")
            return redirect(url_for('login'))

    return render_template('login.html', form=form)


@app.route('/newuser', methods=['GET', 'POST'])
def newuser():
    form = NewUserForm()

    if form.validate_on_submit():
        if mongodb.device_exist(form.deviceid.data) is False:
            flash("Device ID Not Found!")
            return redirect(url_for('newuser'))

        if mongodb.username_exist(form.username.data) is True:
            flash("Username already exists. Please try again.")
            return redirect(url_for('newuser'))

        mongodb.add_user(form.name.data, form.username.data, form.password.data, form.deviceid.data)
        mongodb.remove_device(form.deviceid.data)
        flash("Account Creation Successful. Please login below.")
        return redirect(url_for('login'))

    return render_template('newuser.html', form=form)


@app.route('/<username>/home', methods=['GET', 'POST'])
def userhome(username):
    return render_template('userhome.html', username=username)


if __name__ == '__main__':
    app.run(debug=True)