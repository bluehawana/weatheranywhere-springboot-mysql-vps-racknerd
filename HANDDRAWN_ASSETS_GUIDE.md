# 🎨 Handdrawn Weather Assets Guide

This guide explains how to organize and create handmade weather assets for your artistic weather app.

## 📁 Directory Structure

```
src/main/resources/static/
├── images/
│   └── handdrawn/
│       └── cities/
│           ├── new-york-sketch.png      # Watercolor NYC skyline
│           ├── paris-sketch.png         # Ink drawing of Eiffel Tower
│           ├── london-sketch.png        # Pencil sketch of Big Ben
│           ├── tokyo-sketch.png         # Brush painting of Mount Fuji
│           ├── stockholm-sketch.png     # Watercolor of Gamla Stan
│           ├── singapore-sketch.png     # Ink wash of Marina Bay
│           ├── mohe-sketch.png          # Charcoal winter landscape
│           ├── reykjavik-sketch.png     # Watercolor colorful houses
│           └── default-sketch.png       # Generic city sketch
└── animations/
    └── handdrawn/
        ├── sunny-sketch.gif             # Hand-drawn sun animation
        ├── rain-sketch.gif              # Sketched raindrops
        ├── heavy-rain-sketch.gif        # Dense hand-drawn rain
        ├── snow-sketch.gif              # Delicate snowflake drawings
        ├── heavy-snow-sketch.gif        # Thick snowfall sketch
        ├── clouds-sketch.gif            # Fluffy hand-drawn clouds
        ├── partly-cloudy-sketch.gif     # Sun through sketched clouds
        ├── storm-sketch.gif             # Dramatic lightning sketches
        ├── fog-sketch.gif               # Misty watercolor washes
        ├── drizzle-sketch.gif           # Light pencil rain strokes
        └── wind-sketch.gif              # Swirling wind lines
```

## 🎨 Artistic Styles for City Illustrations

### 1. **Watercolor Style** (Recommended for coastal cities)
- **Cities**: Stockholm, Singapore, Reykjavik
- **Technique**: Soft, flowing colors with wet-on-wet effects
- **Colors**: Muted blues, greens, and earth tones
- **Paper**: Watercolor paper texture overlay

### 2. **Ink Drawing Style** (Perfect for European cities)
- **Cities**: Paris, London
- **Technique**: Fine line work with cross-hatching
- **Colors**: Black ink with selective color highlights
- **Paper**: Cream or off-white background

### 3. **Pencil Sketch Style** (Great for modern cities)
- **Cities**: New York, Tokyo
- **Technique**: Graphite shading and blending
- **Colors**: Grayscale with subtle color accents
- **Paper**: Textured drawing paper

### 4. **Charcoal Style** (Ideal for winter/northern cities)
- **Cities**: Mohe, northern locations
- **Technique**: Bold, dramatic contrasts
- **Colors**: Black and white with blue-gray tones
- **Paper**: Rough paper texture

## 🌦️ Weather Animation Styles

### Rain Animations
- **Light Rain**: Gentle, sparse droplets
- **Heavy Rain**: Dense, diagonal rain lines
- **Style**: Hand-drawn with slight irregularities
- **Duration**: 1-2 second loops

### Snow Animations
- **Light Snow**: Individual snowflakes floating
- **Heavy Snow**: Dense snowfall with wind effects
- **Style**: Delicate, unique snowflake shapes
- **Duration**: 3-5 second loops

### Sun Animations
- **Clear Sky**: Gentle pulsing sun with rays
- **Style**: Warm, hand-drawn radiating lines
- **Duration**: 2-3 second breathing effect

### Cloud Animations
- **Partly Cloudy**: Clouds drifting across
- **Overcast**: Slow-moving cloud layers
- **Style**: Fluffy, organic cloud shapes
- **Duration**: 10-20 second slow loops

## 🖌️ Creation Guidelines

### For City Illustrations (PNG)
1. **Size**: 450x350 pixels (or higher resolution)
2. **Format**: PNG with transparency where needed
3. **Style**: Maintain artistic, hand-drawn feel
4. **Colors**: Use muted, artistic color palettes
5. **Details**: Include recognizable landmarks
6. **Texture**: Add paper/canvas texture overlay

### For Weather Animations (GIF)
1. **Size**: 450x350 pixels to match city illustrations
2. **Format**: GIF with transparency
3. **Frame Rate**: 12-24 fps for smooth animation
4. **Loop**: Seamless looping
5. **Style**: Match the hand-drawn aesthetic
6. **Opacity**: 70-80% for proper blending

## 🎭 Artistic Techniques

### Traditional Media Simulation
- **Watercolor**: Use soft edges, color bleeding effects
- **Ink**: Sharp lines with varying thickness
- **Pencil**: Gradual shading, smudging effects
- **Charcoal**: Bold contrasts, finger-smudged textures

### Digital Enhancement
- **Paper Texture**: Add subtle paper grain overlay
- **Brush Strokes**: Maintain visible brush/pencil marks
- **Color Variation**: Slight color variations for organic feel
- **Edge Quality**: Avoid perfect geometric shapes

## 🔄 Fallback System

If handmade assets aren't available, the system automatically generates:
1. **CSS-based city silhouettes** with SVG
2. **Animated weather effects** using CSS animations
3. **Paper texture backgrounds** with SVG filters
4. **Hand-lettered text** with cursive fonts

## 📝 Asset Naming Convention

### Cities
- Format: `{city-name}-sketch.{extension}`
- Example: `new-york-sketch.png`, `paris-sketch.png`

### Weather
- Format: `{weather-type}-sketch.{extension}`
- Example: `rain-sketch.gif`, `sunny-sketch.gif`

## 🎨 Color Palettes

### Warm Weather
- **Sunny**: #FFD700, #FFA500, #FF8C00
- **Clear**: #87CEEB, #98FB98, #F0E68C

### Cool Weather
- **Rain**: #4682B4, #6495ED, #708090
- **Snow**: #E6E6FA, #B0E0E6, #F0F8FF

### Dramatic Weather
- **Storm**: #483D8B, #2F4F4F, #696969
- **Fog**: #D3D3D3, #C0C0C0, #A9A9A9

## 🚀 Implementation Tips

1. **Start Simple**: Begin with basic sketches and enhance over time
2. **Consistent Style**: Maintain the same artistic style across all assets
3. **Test Blending**: Ensure weather overlays blend well with city backgrounds
4. **Mobile Friendly**: Test on different screen sizes
5. **Performance**: Optimize file sizes for web delivery

## 📱 Responsive Considerations

- **Desktop**: Full 450x350 resolution
- **Tablet**: Scale to fit container
- **Mobile**: Maintain aspect ratio, reduce complexity if needed

This handdrawn approach will give your weather app a unique, artistic personality that stands out from typical weather applications! 🎨✨