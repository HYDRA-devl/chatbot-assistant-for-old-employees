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
          DEFAULT: '#0b5aa6',
          600: '#084c8a'
        },
        ink: '#1f2a37',
        sand: '#f5f2ed',
        mist: '#faf8f4',
        border: '#d8d2c6'
      },
      boxShadow: {
        subtle: '0 1px 2px rgba(17, 24, 39, 0.08), 0 4px 12px rgba(17, 24, 39, 0.06)'
      },
      fontFamily: {
        sans: [
          '"Source Sans 3"',
          'sans-serif'
        ],
        serif: [
          '"Source Serif 4"',
          'serif'
        ]
      }
    }
  },
  plugins: []
};
