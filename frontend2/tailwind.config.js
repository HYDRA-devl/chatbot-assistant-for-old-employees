/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{js,jsx,ts,tsx}'
  ],
  theme: {
    extend: {
      colors: {
        accent: {
          DEFAULT: '#3b82f6', // blue-500
          600: '#2563eb' // blue-600
        }
      },
      boxShadow: {
        subtle: '0 1px 2px rgba(0,0,0,0.06), 0 1px 3px rgba(0,0,0,0.1)'
      },
      fontFamily: {
        sans: [
          'Inter',
          'ui-sans-serif',
          'system-ui',
          '-apple-system',
          'Segoe UI',
          'Roboto',
          'Helvetica Neue',
          'Arial',
          'Noto Sans',
          'Apple Color Emoji',
          'Segoe UI Emoji',
          'Segoe UI Symbol'
        ]
      }
    }
  },
  plugins: []
};

