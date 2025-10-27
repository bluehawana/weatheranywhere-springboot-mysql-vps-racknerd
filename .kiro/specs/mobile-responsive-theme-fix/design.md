# Design Document

## Overview

This design addresses two critical issues in the WeatherAnywhere application:

1. **Mobile Responsiveness**: Weather results and SVG graphics overflow off-screen on mobile devices, making the application unusable on smartphones
2. **Theme Switching Logic**: The automatic theme switching based on local time is not functioning correctly - it should show white theme during 08:00-18:00 and dark theme during 18:00-08:00

The solution involves CSS improvements for responsive design and JavaScript fixes for the theme detection logic.

## Architecture

### Component Structure

The application consists of multiple HTML pages that need responsive fixes:

- `index.html` - Main landing page with ASCII art and weather search
- `enhanced-weather.html` - Modern weather display with icons
- `handdrawn-weather.html` - Artistic weather visualization
- `weatherinfo.html` - Thymeleaf template for weather results

### Theme System Architecture

The theme system uses:

- JavaScript time detection via `Date()` object to get user's local time
- CSS classes (`.dark-theme`) applied to `<body>` element
- CSS variables and media queries for responsive styling
- Interval-based checking (every 60 seconds) for automatic theme switching

## Components and Interfaces

### 1. Responsive CSS Enhancements

**Target Files**: `styles.css`, inline styles in HTML files

**Changes Required**:

- Add mobile-first media queries for viewports < 768px
- Implement flexible container widths using `max-width: 100%`
- Add responsive font sizing using `rem` units with viewport-based scaling
- Constrain SVG and image elements to parent container width
- Adjust padding and margins for mobile screens
- Handle ASCII art display on mobile (hide or scale appropriately)
- Ensure form inputs and buttons are touch-friendly (min 44px height)

**CSS Strategy**:

```css
/* Mobile-first approach */
#weatherDiv {
  width: 95%;
  max-width: 100%;
  padding: 10px;
  overflow-x: hidden;
}

#weatherDiv svg,
#weatherDiv img {
  max-width: 100%;
  height: auto;
}

@media (max-width: 768px) {
  body {
    padding: 10px;
    font-size: 14px;
  }

  .ascii-art {
    display: none; /* Hide on mobile to prevent layout issues */
  }

  input[type="text"] {
    width: 100%;
    max-width: 300px;
  }
}

@media (max-width: 480px) {
  h1 {
    font-size: 1.2rem;
  }

  .ascii-logo {
    font-size: 4px;
  }
}
```

### 2. Theme Switching Logic Fix

**Target File**: `index.html` (inline JavaScript)

**Current Issue**:
The theme detection logic has a boundary condition error. The current code checks:

```javascript
if (hour >= 18 || hour < 8)
```

This should trigger dark theme from 18:00-07:59, but the default state or initialization may be causing issues.

**Root Cause Analysis**:

- The function `applyThemeBasedOnTime()` is called on page load
- If the hour is 8 (8:00-8:59), the condition `hour < 8` is false and `hour >= 18` is false
- Therefore, `classList.remove('dark-theme')` should execute
- However, the CSS may have dark theme as default, or there's a race condition

**Solution**:

1. Ensure white theme is the default in CSS (no `.dark-theme` class initially)
2. Explicitly set the theme on page load before any rendering
3. Fix the hour boundary to be inclusive: 18:00-07:59 = dark, 08:00-17:59 = white
4. Add defensive coding to handle edge cases

**Fixed JavaScript**:

```javascript
function applyThemeBasedOnTime() {
  const now = new Date();
  const hour = now.getHours();

  // Dark theme: 18:00 (6 PM) to 07:59 (7:59 AM)
  // White theme: 08:00 (8 AM) to 17:59 (5:59 PM)
  if (hour >= 18 || hour < 8) {
    document.body.classList.add("dark-theme");
  } else {
    document.body.classList.remove("dark-theme");
  }
}

// Apply theme immediately before page renders
applyThemeBasedOnTime();

// Apply theme on DOM ready as well
document.addEventListener("DOMContentLoaded", function () {
  applyThemeBasedOnTime();
});

// Check and update theme every minute
setInterval(applyThemeBasedOnTime, 60000);
```

### 3. Marketing Copy Update

**Target File**: `index.html`

**Current Text**:

```html
<p
  style="font-family: 'Courier New', monospace; color: #00ff00; margin: 10px 0;"
>
  ⚡ No Flash | No Java | Pure HTML Magic ⚡
</p>
```

**Issue**: States "No Java" but this is a Java Spring Boot application

**Proposed Alternatives** (maintaining 90s retro aesthetic):

1. `⚡ No Flash | No Plugins | Pure HTML Magic ⚡`
2. `⚡ No Flash | No ActiveX | Pure Web Magic ⚡`
3. `⚡ Server-Side Powered | Client-Side Simple | Pure HTML Magic ⚡`
4. `⚡ No Flash | No Applets | Pure HTML Magic ⚡`

**Recommendation**: Option 1 or 4, as they maintain the retro feel while being technically accurate.

## Data Models

No data model changes required. This is purely a frontend presentation fix.

## Error Handling

### Theme Switching Errors

**Scenario**: Browser doesn't support `Date()` or returns invalid time
**Handling**: Default to white theme (daytime) as it's more universally readable

```javascript
function applyThemeBasedOnTime() {
  try {
    const now = new Date();
    const hour = now.getHours();

    // Validate hour is a number
    if (isNaN(hour) || hour < 0 || hour > 23) {
      console.warn("Invalid hour detected, defaulting to white theme");
      document.body.classList.remove("dark-theme");
      return;
    }

    if (hour >= 18 || hour < 8) {
      document.body.classList.add("dark-theme");
    } else {
      document.body.classList.remove("dark-theme");
    }
  } catch (error) {
    console.error("Error applying theme:", error);
    document.body.classList.remove("dark-theme"); // Default to white theme
  }
}
```

### Responsive Layout Errors

**Scenario**: Content still overflows on very small screens (< 320px)
**Handling**: Use `overflow-x: hidden` on body and containers, with `overflow-y: auto` to allow vertical scrolling

## Testing Strategy

### Manual Testing Checklist

**Mobile Responsiveness**:

1. Test on physical devices: iPhone SE (375px), iPhone 12 (390px), Samsung Galaxy (360px)
2. Test in browser DevTools with responsive mode at 320px, 375px, 414px, 768px
3. Search for "Sanya", "Hawaii", "Tokyo" and verify:
   - All weather results visible without horizontal scroll
   - SVG graphics scale appropriately
   - Text remains readable
   - Buttons are touch-friendly
4. Test landscape orientation on mobile devices
5. Verify ASCII art either hides or scales appropriately on mobile

**Theme Switching**:

1. Test at different times of day:
   - 07:59 - should show dark theme
   - 08:00 - should show white theme
   - 08:19 - should show white theme (user's reported time)
   - 17:59 - should show white theme
   - 18:00 - should show dark theme
   - 23:59 - should show dark theme
2. Test theme persistence when navigating between pages
3. Test automatic switching by keeping page open across time boundaries
4. Test in different timezones (change system time)
5. Verify default theme is white when JavaScript fails

**Cross-Browser Testing**:

- Chrome (desktop & mobile)
- Firefox (desktop & mobile)
- Safari (desktop & iOS)
- Edge (desktop)

### Automated Testing

While this is primarily a visual/CSS fix, we can add basic validation:

**JavaScript Unit Tests** (if test framework exists):

```javascript
describe("Theme Switching", () => {
  it("should apply dark theme between 18:00 and 07:59", () => {
    // Mock Date to return 20:00
    // Call applyThemeBasedOnTime()
    // Assert body has 'dark-theme' class
  });

  it("should apply white theme between 08:00 and 17:59", () => {
    // Mock Date to return 10:00
    // Call applyThemeBasedOnTime()
    // Assert body does not have 'dark-theme' class
  });
});
```

**Responsive Testing**:

- Use browser automation (Selenium/Playwright) to capture screenshots at different viewport sizes
- Compare screenshots to ensure no horizontal overflow
- Verify all interactive elements are within viewport bounds

## Implementation Notes

### CSS Best Practices

- Use `box-sizing: border-box` globally to simplify width calculations
- Avoid fixed widths; use `max-width` with percentage-based widths
- Use `rem` for font sizes to respect user's browser settings
- Test with browser zoom at 150% and 200%

### JavaScript Best Practices

- Call theme function before DOM renders to prevent flash of wrong theme
- Use try-catch for defensive programming
- Add console logging for debugging (can be removed in production)
- Consider using `localStorage` to remember user's theme preference override (future enhancement)

### Performance Considerations

- CSS changes have minimal performance impact
- Theme checking every 60 seconds is lightweight
- No additional HTTP requests required
- No impact on server-side Java code

## Future Enhancements

1. **User Theme Override**: Allow users to manually toggle theme regardless of time
2. **System Theme Detection**: Use `prefers-color-scheme` media query to respect OS settings
3. **Smooth Theme Transitions**: Add CSS transitions when switching themes
4. **Progressive Web App**: Add viewport meta tags and service worker for better mobile experience
5. **Touch Gestures**: Add swipe gestures for mobile navigation
