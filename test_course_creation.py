#!/usr/bin/env python3
import requests
import re
import sys
from urllib.parse import urljoin
from bs4 import BeautifulSoup
import time

BASE_URL = 'http://localhost:8082'

# Create a session to maintain cookies
session = requests.Session()

try:
    print("[1/4] Waiting for server to be ready...")
    time.sleep(5)
    
    print("[2/4] Logging in as admin...")
    # Get login page to extract CSRF token
    login_page = session.get(f'{BASE_URL}/login', timeout=10)
    
    if login_page.status_code != 200:
        print(f"ERROR: Could not get login page. Status: {login_page.status_code}")
        sys.exit(1)
    
    # Extract CSRF token
    csrf_match = re.search(r'name="_csrf"\s+value="([^"]+)"', login_page.text)
    if not csrf_match:
        print("ERROR: Could not find CSRF token")
        sys.exit(1)
    
    csrf_token = csrf_match.group(1)
    print(f"   CSRF Token found: {csrf_token[:20]}...")
    
    # Login
    login_data = {
        'username': 'admin',
        'password': 'admin123',
        '_csrf': csrf_token
    }
    
    response = session.post(
        f'{BASE_URL}/login',
        data=login_data,
        allow_redirects=True,
        timeout=10
    )
    
    if 'login' in response.url.lower():
        print("ERROR: Login failed!")
        print(f"   Response URL: {response.url}")
        if 'error' in response.text.lower():
            print("   Error message found in response")
        sys.exit(1)
    
    print("   Login successful!")
    
    print("[3/4] Getting course creation form...")
    # Get create form
    form_page = session.get(f'{BASE_URL}/admin/courses/create', timeout=10)
    
    if form_page.status_code != 200:
        print(f"ERROR: Could not get form page. Status: {form_page.status_code}")
        sys.exit(1)
    
    # Extract new CSRF token for form submission
    csrf_match = re.search(r'name="_csrf"\s+value="([^"]+)"', form_page.text)
    if csrf_match:
        csrf_token = csrf_match.group(1)
    
    print("   Form retrieved successfully!")
    
    print("[4/4] Submitting course creation form...")
    # Prepare form data
    form_data = {
        'name': 'Test Course - Java Advanced',
        'credits': '3',
        'lecturer': 'Prof. Nguyen Van A',
        'categoryId': '1',  # Make sure this category exists
        '_csrf': csrf_token
    }
    
    # Prepare file upload
    files = {
        'imageFile': ('test.jpg', b'\xFF\xD8\xFF\xE0', 'image/jpeg')
    }
    
    # Submit form
    submit_response = session.post(
        f'{BASE_URL}/admin/courses',
        data=form_data,
        files=files,
        allow_redirects=False,
        timeout=10
    )
    
    print(f"   Response Status: {submit_response.status_code}")
    print(f"   Response Location: {submit_response.headers.get('Location', 'N/A')}")
    
    if submit_response.status_code == 302:
        redirect_url = submit_response.headers.get('Location', '')
        if 'created' in redirect_url:
            print("\n✓ SUCCESS! Course created successfully!")
            print(f"  Redirected to: {redirect_url}")
            sys.exit(0)
        else:
            print(f"\n✗ FAILED! Unexpected redirect: {redirect_url}")
            sys.exit(1)
    elif submit_response.status_code == 200:
        # Check if form is being re-displayed (indicates validation error)
        if 'course-form' in submit_response.text or 'form' in submit_response.text.lower():
            print("\n✗ FAILED! Form validation error detected")
            # Try to extract error messages
            if 'Vui long chon danh muc' in submit_response.text:
                print("  Error: categoryId validation still failing")
            sys.exit(1)
        else:
            print(f"\n? Status 200 but unexpected response:\n{submit_response.text[:500]}")
            sys.exit(1)
    else:
        print(f"\n✗ FAILED! Unexpected status code: {submit_response.status_code}")
        print(f"Response preview: {submit_response.text[:500]}")
        sys.exit(1)
        
except requests.exceptions.ConnectionError as e:
    print(f"ERROR: Could not connect to server at {BASE_URL}")
    print(f"Details: {e}")
    sys.exit(1)
except Exception as e:
    print(f"ERROR: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)
