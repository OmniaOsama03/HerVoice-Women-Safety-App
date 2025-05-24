const express = require('express');
const nodemailer = require('nodemailer');
const cors = require('cors');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

const otpStore = {}; // { email: otp }

app.post('/send-otp', async (req, res) => 
    {
    const { email } = req.body;

    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    otpStore[email] = otp;

    try 
    {
        const transporter = nodemailer.createTransport({
            service: 'gmail',
            auth: 
            {
                user: process.env.EMAIL_USER,
                pass: process.env.EMAIL_PASS
            }
        });

        await transporter.sendMail({
            from: `"HerVoice" <${process.env.EMAIL_USER}>`,
            to: email,
            subject: "Hervoice OTP Code",
            text: `Welcome to the app! Your OTP code is ${otp}`,
        });

        res.status(200).json({ message: "OTP sent" });

    } catch (err) 
    {
        console.error(err);
        res.status(500).json({ message: "Failed to send OTP" });
    }
});

app.post('/verify-otp', (req, res) => 
    {
    const { email, otp } = req.body;
    if (otpStore[email] === otp) 
    {
        delete otpStore[email]; //To make sure it's used once
        return res.status(200).json({ message: "OTP verified" });

    } else 
    {
        return res.status(400).json({ message: "Invalid OTP" });
    }
});

const PORT = process.env.PORT || 3000;

app.listen(3000, '0.0.0.0', () => {
    console.log('Server running on port 3000');
  });
  