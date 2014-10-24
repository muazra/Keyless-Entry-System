__author__ = 'Muaz'

from flask import Flask, render_template, session
from flask.ext.bootstrap import Bootstrap
from flask.ext.wtf import Form
from wtforms import StringField, SubmitField, PasswordField
from wtforms.validators import DataRequired

app = Flask(__name__)
app.config['SECRET_KEY'] = 'Project KES'
bootstrap = Bootstrap(app)


class LoginForm(Form):
    username = StringField('Username', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    submit = SubmitField('Submit')


class NewUserForm(Form):
    firstname = StringField('First Name', validators=[DataRequired()])
    lastname = StringField('Last Name', validators=[DataRequired()])
    username = StringField('Username', validators=[DataRequired()])
    password = PasswordField('Password', validators=[DataRequired()])
    deviceid = StringField('Unique Device ID', validators=[DataRequired])
    submit = SubmitField('Submit')

@app.route('/', methods=['GET', 'POST'])
def login():
    form = LoginForm()

    if form.validate_on_submit():
        session['username'] = form.username.data.capitalize()
        session['password'] = form.password.data

    return render_template('login.html', form=form)


@app.route('/newuser', methods=['GET', 'POST'])
def newuser():
    form = NewUserForm()

    if form.validate_on_submit():
        session['firstname'] = form.firstname.data.capitalize()
        session['lastname'] = form.lastname.data.capitalize()
        session['username'] = form.username.data.capitalize()
        session['password'] = form.password.data
        session['deviceid'] = form.deviceid.data

    return render_template('newuser.html', form=form)

if __name__ == '__main__':
    app.run(debug=True)