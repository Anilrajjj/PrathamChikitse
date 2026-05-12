$basePath = "C:\Users\Anil Kumar\OneDrive\Desktop\app\PrathamChikitse\app\src\main\res"
$sourceImage = "C:\Users\Anil Kumar\OneDrive\Desktop\app\prathamachikitse.png"
$sizes = @{
    "mipmap-mdpi" = 48
    "mipmap-hdpi" = 72
    "mipmap-xhdpi" = 96
    "mipmap-xxhdpi" = 144
    "mipmap-xxxhdpi" = 192
}

Add-Type -AssemblyName System.Drawing

if (-not (Test-Path $sourceImage)) {
    Write-Host "ERROR: Source image not found at $sourceImage"
    exit 1
}

foreach ($dir in $sizes.Keys) {
    $size = $sizes[$dir]
    $path = "$basePath\$dir"
    
    if (-not (Test-Path $path)) {
        New-Item -ItemType Directory -Force -Path $path | Out-Null
    }
    
    # Load source image and resize
    $src = [System.Drawing.Image]::FromFile($sourceImage)
    
    # Create ic_launcher.png (resized to exact size)
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $g.DrawImage($src, 0, 0, $size, $size)
    $bmp.Save("$path\ic_launcher.png", [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    $g.Dispose()
    
    # Create ic_launcher_round.png (clip to circle first)
    $bmp2 = New-Object System.Drawing.Bitmap $size, $size
    $g2 = [System.Drawing.Graphics]::FromImage($bmp2)
    $g2.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g2.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $g2.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    
    # Create circular clipping path
    $clipPath = New-Object System.Drawing.Drawing2D.GraphicsPath
    $clipPath.AddEllipse(0, 0, $size - 1, $size - 1)
    $g2.SetClip($clipPath)
    $g2.DrawImage($src, 0, 0, $size, $size)
    $g2.ResetClip()
    $bmp2.Save("$path\ic_launcher_round.png", [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp2.Dispose()
    $g2.Dispose()
    $clipPath.Dispose()
    
    $src.Dispose()
    
    Write-Host "Created $dir ($size x $size)"
}

# Also create splash icon drawable
$drawablePath = "$basePath\drawable"
if (-not (Test-Path $drawablePath)) {
    New-Item -ItemType Directory -Force -Path $drawablePath | Out-Null
}
$src = [System.Drawing.Image]::FromFile($sourceImage)
$bmp = New-Object System.Drawing.Bitmap 108, 108
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$g.DrawImage($src, 0, 0, 108, 108)
$bmp.Save("$drawablePath\ic_splash.png", [System.Drawing.Imaging.ImageFormat]::Png)
$bmp.Dispose()
$g.Dispose()
$src.Dispose()
Write-Host "Created splash icon (108 x 108)"

Write-Host "`nAll icons created successfully!"
