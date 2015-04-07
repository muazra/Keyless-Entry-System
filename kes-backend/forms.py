__author__ = 'Muaz'

from flask.ext.wtf import Form
from flask_wtf.file import FileField, FileAllowed, FileRequired
from wtforms import StringField, SubmitField, PasswordField
from wtforms.validators import DataRequired


class LoginForm(Form):
    username = StringField('Username', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    submit = SubmitField('Submit')


class NewAdminForm(Form):
    name = StringField('Name', validators=[DataRequired()])
    username = StringField('Username', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    deviceid = StringField('Device ID', validators=[DataRequired()])
    photo = FileField('Photo', validators=[
        FileRequired(),
        FileAllowed(['jpeg', 'jpg', 'png'], 'Images only! (jpeg, jpg, or png)')
    ])
    submit = SubmitField('Submit')


class NewUserForm(Form):
    name = StringField('Name', validators=[DataRequired()])
    username = StringField('Username', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    photo = FileField('Photo', validators=[
        FileRequired(),
        FileAllowed(['jpeg', 'jpg', 'png'], 'Images only! (jpeg, jpg, or png)')
    ])
    submit = SubmitField('Submit')


class NewGuestForm(Form):
    name = StringField('Name', validators=[DataRequired()])
    photo = FileField('Photo', validators=[
        FileRequired(),
        FileAllowed(['jpeg', 'jpg', 'png'], 'Images only! (jpeg, jpg, or png)')
    ])
    submit = SubmitField('Submit')


class PhotoForm(Form):
    photo = FileField('Upload New Photo:', validators=[
        FileRequired(),
        FileAllowed(['jpeg', 'jpg', 'png'], 'Images only! (jpeg, jpg, or png)')
    ])
    submit = SubmitField('Submit')