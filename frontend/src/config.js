// Central API Base URL Configuration
// Automatically selects localhost for local development, or live AWS EC2 IP when deployed on AWS S3/CloudFront.
export const API_BASE = (typeof window !== 'undefined' && (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'))
  ? 'http://localhost:8085'
  : 'http://35.154.223.86:8085';
